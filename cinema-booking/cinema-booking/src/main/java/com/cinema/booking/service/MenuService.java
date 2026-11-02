package com.cinema.booking.service;

import com.cinema.booking.model.Menu;
import com.cinema.booking.repository.MenuRepository;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
public class MenuService {

    private final MenuRepository menuRepository;

    public MenuService(MenuRepository menuRepository) {
        this.menuRepository = menuRepository;
    }

    public List<Menu> getMenuTree() {
        List<Menu> allMenus = menuRepository.findByActiveTrueOrderBySortOrderAsc();
        List<Menu> rootMenus = new ArrayList<>();
        for (Menu menu : allMenus) {
            if (menu.getParentId() == null) {
                menu.setChildren(findChildren(menu, allMenus));
                rootMenus.add(menu);
            }
        }
        return rootMenus;
    }

    private List<Menu> findChildren(Menu parent, List<Menu> allMenus) {
        List<Menu> children = new ArrayList<>();
        for (Menu menu : allMenus) {
            if (parent.getMenuId().equals(menu.getParentId())) {
                menu.setChildren(findChildren(menu, allMenus));
                children.add(menu);
            }
        }
        return children;
    }
}