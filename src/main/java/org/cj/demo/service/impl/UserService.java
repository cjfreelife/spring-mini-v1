package org.cj.demo.service.impl;

import org.cj.mvcframework.annotion.CjService;
import org.cj.demo.dto.User;
import org.cj.demo.service.IUserService;

@CjService("userService")
public class UserService implements IUserService {
    public void addUser(User user) {
        System.out.println(user);
    }
}
