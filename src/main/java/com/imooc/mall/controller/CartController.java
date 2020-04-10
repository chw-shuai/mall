package com.imooc.mall.controller;

import com.imooc.mall.consts.MallConst;
import com.imooc.mall.form.CartAddForm;
import com.imooc.mall.form.CartUpdateForm;
import com.imooc.mall.pojo.User;
import com.imooc.mall.service.ICartService;
import com.imooc.mall.vo.CartVo;
import com.imooc.mall.vo.ResponseVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

@RestController
@Slf4j
public class CartController {

    @Autowired
    ICartService cartService;


    @GetMapping("/carts")
    ResponseVo<CartVo> list(@Valid @RequestBody HttpSession session) {
        User user = (User) session.getAttribute(MallConst.CURRENT_USER);
        Integer uid = user.getId();
        return cartService.list(uid);
    }

    @PostMapping("/carts")
    ResponseVo<CartVo> add(@Valid @RequestBody CartAddForm cartAddForm, HttpSession session) {
        User user = (User) session.getAttribute(MallConst.CURRENT_USER);
        Integer uid = user.getId();
        return cartService.add(uid, cartAddForm);
    }

    @PutMapping("/carts/{productId}")
    ResponseVo<CartVo> update(@Valid @RequestBody CartUpdateForm cartUpdateForm, HttpSession session,
                              @PathVariable Integer productId) {
        User user = (User) session.getAttribute(MallConst.CURRENT_USER);
        Integer uid = user.getId();
        return cartService.update(uid, productId, cartUpdateForm);
    }

    @DeleteMapping("/carts/{productId}")
    ResponseVo<CartVo> delete(@Valid @RequestBody HttpSession session,
                              @PathVariable Integer productId) {
        User user = (User) session.getAttribute(MallConst.CURRENT_USER);
        Integer uid = user.getId();
        return cartService.delete(uid, productId);
    }

    @PutMapping("/carts/selectAll")
    ResponseVo<CartVo> selectAll(@Valid @RequestBody HttpSession session) {
        User user = (User) session.getAttribute(MallConst.CURRENT_USER);
        Integer uid = user.getId();
        return cartService.selectAll(uid);
    }

    @PutMapping("/carts/unSelectAll")
    ResponseVo<CartVo> unSelectAll(@Valid @RequestBody HttpSession session) {
        User user = (User) session.getAttribute(MallConst.CURRENT_USER);
        Integer uid = user.getId();
        return cartService.unSelectAll(uid);
    }

    @GetMapping("/carts/products/sum")
    ResponseVo<Integer> sum(@Valid @RequestBody HttpSession session) {
        User user = (User) session.getAttribute(MallConst.CURRENT_USER);
        Integer uid = user.getId();
        return cartService.sum(uid);
    }
}
