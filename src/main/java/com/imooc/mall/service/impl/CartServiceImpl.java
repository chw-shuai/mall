package com.imooc.mall.service.impl;

import com.google.gson.Gson;
import com.imooc.mall.dao.ProductMapper;
import com.imooc.mall.enums.ProductStatusEnum;
import com.imooc.mall.form.CartAddForm;
import com.imooc.mall.form.CartUpdateForm;
import com.imooc.mall.pojo.Cart;
import com.imooc.mall.pojo.Product;
import com.imooc.mall.service.ICartService;
import com.imooc.mall.vo.CartProductVo;
import com.imooc.mall.vo.CartVo;
import com.imooc.mall.vo.ResponseVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.*;

import static com.imooc.mall.enums.ResponseEnum.*;


@Service
@Slf4j
public class CartServiceImpl implements ICartService {

    private final static String CART_REDIS_KEY_TEMPLATE = "cart_%d";

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private Gson gson;

    @Override
    public ResponseVo<CartVo> add(Integer uid, CartAddForm cartAddForm) {

        Integer quantity = 1;

        //查询商品是否存在
        Product product = productMapper.selectByPrimaryKey(cartAddForm.getProductId());
        if (product == null) {
            return ResponseVo.error(PRODUCT_NOT_EXIST);
        }

        //查询商品是否是在售状态
        if (!product.getStatus().equals(ProductStatusEnum.ON_SALE.getCode())) {
            return ResponseVo.error(PRODUCT_OFF_SALE_OR_DELETE);
        }

        //查询商品库存
        if (product.getStock() < 0) {
            return ResponseVo.error(PRODUCT_STOCK_ERROR);
        }

        //写到Redis
        //key cart_1
        //redis key
        HashOperations<String, String, String> opsForHash = redisTemplate.opsForHash();
        //redis H
        String H = String.format(CART_REDIS_KEY_TEMPLATE, uid);
        //redis HK
        String HV = String.valueOf(product.getId());
        Cart cart;
        //取出redis数据
        String value = opsForHash.get(H, HV);
        //根据H和HK取出数据判断是否为空
        if (StringUtils.isEmpty(value)) {
            //如果为空 新建对象
            cart = new Cart(product.getId(), quantity, cartAddForm.getSelected());
        } else {
            //如果不为空  数量+1
            cart = gson.fromJson(value, Cart.class);
            cart.setQuantity(cart.getQuantity() + quantity);
        }
        opsForHash.put(H, HV, gson.toJson(cart));
        return list(uid);
    }

    @Override
    public ResponseVo<CartVo> list(Integer uid) {
        HashOperations<String, String, String> opsForHash = redisTemplate.opsForHash();
        //redis key
        String H = String.format(CART_REDIS_KEY_TEMPLATE, uid);
        //根据key获取全部 key：value
        Map<String, String> entries = opsForHash.entries(H);
        //entrySet该方法返回值就是这个map中各个键值对映射关系的集合
        Set<Map.Entry<String, String>> entrySet = entries.entrySet();
        //List集合 存放CartProductVo
        List<CartProductVo> cartProductVoList = new ArrayList<>();
        //构造返回对象CartVo
        CartVo cartVo = new CartVo();
        //是否是全选
        Boolean selectedAll = true;
        //全部商品数量
        Integer cartTotalQuantity = 0;
        //全部商品总价格
        BigDecimal cartTotalPrice = BigDecimal.ZERO;
        Set<Integer> set = new HashSet<>();
        for (Map.Entry<String, String> stringEntry : entrySet) {
            //获取Key
            Integer productId = Integer.valueOf(stringEntry.getKey());
            //获取value 反序列化成Cart对象
            Cart cart = gson.fromJson(stringEntry.getValue(), Cart.class);
            //TODO 优化 访问数据库频繁  使用mysql 里面的IN
            //根据productId查询数据库
            set.add(productId);
            List<Product> productList = productMapper.selectByProductIdSet(set);
            for (Product product : productList) {
                if (product != null) {
                    //商品总价
                    BigDecimal productTotalPrice = (product.getPrice().multiply(BigDecimal.valueOf(cart.getQuantity())));
                    //构造CartProductVo
                    CartProductVo cartProductVo = new CartProductVo(productId, cart.getQuantity(), product.getName(),
                            product.getSubtitle(), product.getMainImage(), product.getPrice(),
                            product.getStatus(), productTotalPrice, product.getStock(),
                            cart.getProductSelected());
                    cartProductVoList.add(cartProductVo);
                    //假如有一个商品未被选中  全选就为false
                    if (!cartProductVo.getProductSelected()) {
                        selectedAll = false;
                    }
                    //只计算选中物品的总价
                    if (cartProductVo.getProductSelected()) {
                        cartTotalPrice = cartTotalPrice.add(cartProductVo.getProductTotalPrice());
                    }
                }
            }
            cartTotalQuantity += cart.getQuantity();
        }
        //是否全选
        cartVo.setSelectedAll(selectedAll);
        //购物车总数量
        cartVo.setCartTotalQuantity(cartTotalQuantity);
        //购物车商品总价格
        cartVo.setCartTotalPrice(cartTotalPrice);
        //添加list集合
        cartVo.setCartProductVoList(cartProductVoList);
        return ResponseVo.success(cartVo);
    }

    @Override
    public ResponseVo<CartVo> update(Integer uid, Integer productId, CartUpdateForm cartUpdateForm) {
        //redis key
        HashOperations<String, String, String> opsForHash = redisTemplate.opsForHash();
        //redis H
        String H = String.format(CART_REDIS_KEY_TEMPLATE, uid);
        //redis HK
        String HV = String.valueOf(productId);
        //取出redis数据
        String value = opsForHash.get(H, HV);
        //根据H和HK取出数据判断是否为空
        if (StringUtils.isEmpty(value)) {
            return ResponseVo.error(CART_PRODUCT_NOT_EXIST);
        }
        Cart cart = gson.fromJson(value, Cart.class);
        //如果不为空  修改数量
        if (cart.getQuantity() != null && cart.getQuantity() > 0) {
            cart.setQuantity(cartUpdateForm.getQuantity());
        }
        cart.setProductSelected(cartUpdateForm.getSelected());

        opsForHash.put(H, HV, gson.toJson(cart));

        return list(uid);
    }

    @Override
    public ResponseVo<CartVo> delete(Integer uid, Integer productId) {
        //redis key
        HashOperations<String, String, String> opsForHash = redisTemplate.opsForHash();
        //redis H
        String H = String.format(CART_REDIS_KEY_TEMPLATE, uid);
        //redis HK
        String HV = String.valueOf(productId);
        //取出redis数据
        String value = opsForHash.get(H, HV);
        //根据H和HK取出数据判断是否为空
        if (StringUtils.isEmpty(value)) {
            return ResponseVo.error(CART_PRODUCT_NOT_EXIST);
        }
        opsForHash.delete(H, HV);

        return list(uid);
    }


    @Override
    public ResponseVo<CartVo> selectAll(Integer uid) {
        HashOperations<String, String, String> opsForHash = redisTemplate.opsForHash();
        //redis key
        String H = String.format(CART_REDIS_KEY_TEMPLATE, uid);
        List<Cart> cartList = listForCart(uid);
        for (Cart cart : cartList) {
            cart.setProductSelected(true);
            opsForHash.put(H, String.valueOf(cart.getProductId()), gson.toJson(cart));

        }
        return list(uid);
    }

    @Override
    public ResponseVo<CartVo> unSelectAll(Integer uid) {
        HashOperations<String, String, String> opsForHash = redisTemplate.opsForHash();
        //redis key
        String H = String.format(CART_REDIS_KEY_TEMPLATE, uid);
        List<Cart> cartList = listForCart(uid);
        for (Cart cart : cartList) {
            cart.setProductSelected(false);
            opsForHash.put(H, String.valueOf(cart.getProductId()), gson.toJson(cart));

        }
        return list(uid);
    }

    @Override
    public ResponseVo<Integer> sum(Integer uid) {
        List<Cart> cartList = listForCart(uid);
        Integer sum = 0;
        for (Cart cart : cartList) {
            sum += cart.getQuantity();
        }
        return ResponseVo.success(sum);
    }

    public List<Cart> listForCart(Integer uid) {
        HashOperations<String, String, String> opsForHash = redisTemplate.opsForHash();
        //redis key
        String H = String.format(CART_REDIS_KEY_TEMPLATE, uid);
        //根据key获取全部 key：value
        Map<String, String> entries = opsForHash.entries(H);
        //entrySet该方法返回值就是这个map中各个键值对映射关系的集合
        Set<Map.Entry<String, String>> entrySet = entries.entrySet();
        List<Cart> cartList = new ArrayList<>();
        for (Map.Entry<String, String> stringStringEntry : entrySet) {
            String value = stringStringEntry.getValue();
            Cart cart = gson.fromJson(value, Cart.class);
            cartList.add(cart);
        }
        return cartList;
    }
}
