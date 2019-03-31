package org.cj.demo.controller;

import org.cj.mvcframework.annotion.CjAutowired;
import org.cj.mvcframework.annotion.CjController;
import org.cj.mvcframework.annotion.CjParameter;
import org.cj.mvcframework.annotion.CjRequestMapping;
import org.cj.demo.dto.User;
import org.cj.demo.service.IUserService;

@CjController
@CjRequestMapping("user")
public class UserController {
    @CjAutowired
    private IUserService userService;

    @CjRequestMapping("addUser")
    public void addUser(@CjParameter("userName") String userName, @CjParameter("age") Integer age) {
        User user = new User();
        user.setName(userName);
        user.setAge(age);
        userService.addUser(user);
    }
}
