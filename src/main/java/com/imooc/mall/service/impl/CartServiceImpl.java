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
        String redisKey  = String.format(CART_REDIS_KEY_TEMPLATE, uid);
        Map<String, String> entries = opsForHash.entries(redisKey);

        boolean selectAll = true;
        Integer cartTotalQuantity = 0;
        BigDecimal cartTotalPrice = BigDecimal.ZERO;
        CartVo cartVo = new CartVo();
        List<CartProductVo> cartProductVoList = new ArrayList<>();
        for (Map.Entry<String, String> entry : entries.entrySet()) {
            Integer productId = Integer.valueOf(entry.getKey());
            Cart cart = gson.fromJson(entry.getValue(), Cart.class);

            //TODO 需要优化，使用mysql里的in
            Product product = productMapper.selectByPrimaryKey(productId);
            if (product != null) {
                CartProductVo cartProductVo = new CartProductVo(productId,
                        cart.getQuantity(),
                        product.getName(),
                        product.getSubtitle(),
                        product.getMainImage(),
                        product.getPrice(),
                        product.getStatus(),
                        product.getPrice().multiply(BigDecimal.valueOf(cart.getQuantity())),
                        product.getStock(),
                        cart.getProductSelected()
                );
                cartProductVoList.add(cartProductVo);

                if (!cart.getProductSelected()) {
                    selectAll = false;
                }

                //计算总价(只计算选中的)
                if (cart.getProductSelected()) {
                    cartTotalPrice = cartTotalPrice.add(cartProductVo.getProductTotalPrice());
                }
            }

            cartTotalQuantity += cart.getQuantity();
        }

        //有一个没有选中，就不叫全选
        cartVo.setSelectedAll(selectAll);
        cartVo.setCartTotalQuantity(cartTotalQuantity);
        cartVo.setCartTotalPrice(cartTotalPrice);
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
        Integer sum = listForCart(uid).stream()
                .map(Cart::getQuantity)
                .reduce(0, Integer::sum);
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
