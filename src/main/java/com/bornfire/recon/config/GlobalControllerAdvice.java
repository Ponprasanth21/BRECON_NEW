package com.bornfire.recon.config;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.bornfire.recon.entities.ACCESS_AND_ROLES_REPO;
import com.bornfire.recon.entities.USER_PROFILE_REPO;

@ControllerAdvice
public class GlobalControllerAdvice {

  @Autowired
  private ACCESS_AND_ROLES_REPO accessAndRoleRepo;

  @Autowired
  private USER_PROFILE_REPO userprofileRepo;

  @ModelAttribute
  public void addMenus(Model model, HttpServletRequest req) {
    String userid = (String) req.getSession().getAttribute("USERID");

    List<String> menus = Collections.emptyList();
    if (userid != null) {
      String roleId = userprofileRepo.getRoleID(userid);
      if (roleId != null) {
        menus = accessAndRoleRepo.findById(roleId)
            .map(role -> Arrays.stream(role.getMenulist().split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList()))
            .orElse(Collections.emptyList());
      }
    }

    model.addAttribute("menus", menus);
  }
}
