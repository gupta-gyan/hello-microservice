// src/main/java/com/example/demo/controller/HelloController.java

package com.imesomeone.hello.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloWorldController {

  @GetMapping("/hello")
  public String sayHello() {
    return "Hello from your microservice!";
  }
}
