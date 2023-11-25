package com.sky.service.impl;

import com.sky.dto.DishDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.mapper.DishMapper;
import com.sky.mapper.DishFlavorMapper;
import com.sky.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class DishServiceImpl implements DishService {

    @Autowired
    DishMapper dishMapper;

    @Autowired
    DishFlavorMapper dishFlavorMapper;

    /**
     * 新增菜品和对应口味
     * @param dishDTO
     */
    @Transactional//开启事务管理
    public void saveWithFlavor(DishDTO dishDTO) {
        //创建Dish实体类
        Dish dish=new Dish();

        //对象属性复制
        BeanUtils.copyProperties(dishDTO,dish);

        //向菜品插入一条数据
        dishMapper.insert(dish);

        //获取insert语句生成的主键值
        Long dishId=dish.getId();

        //获取口味数组并且给每个口味实体添加菜品id
        List<DishFlavor> flavors = dishDTO.getFlavors();
        flavors.forEach(dishFlavor -> {
            dishFlavor.setDishId(dishId);
        });

        //口味不为空时
        if(flavors!=null && flavors.size()>0){
            //向口味插入n条数据
            dishFlavorMapper.insertBatch(flavors);
        }
    }
}
