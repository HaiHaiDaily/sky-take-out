package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.entity.Employee;
import com.sky.entity.Setmeal;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishMapper;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class DishServiceImpl implements DishService {

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private DishFlavorMapper dishFlavorMapper;

    @Autowired
    private SetmealDishMapper setmealDishMapper;

    @Autowired
    private SetmealMapper setmealMapper;


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

    /**
     * 菜品分页查询
     * @param dishPageQueryDTO
     * @return
     */
    public PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO) {
        //开始分页
        PageHelper.startPage(dishPageQueryDTO.getPage(),dishPageQueryDTO.getPageSize());
        //使用VO接收数据
        Page<DishVO> page=dishMapper.pageQuery(dishPageQueryDTO);
        return new PageResult(page.getTotal(),page.getResult());
    }

    /**
     * 菜品批量删除
     * @param ids
     */
    @Transactional
    public void deleteBatch(List<Long> ids) {
        //判断是否是起售状态-->不可删
        for (Long id : ids){
            Dish dish=dishMapper.getById(id);
            if(dish.getStatus() == StatusConstant.ENABLE){
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
        }

        //判断是否是有关联的套餐-->不可删
        //获得与菜品关联的套餐
        List<Long> setmealIds=setmealDishMapper.getSetmealIdsByDishIds(ids);
        if(setmealIds !=null && setmealIds.size()>0){
            throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }

        /*//删除菜品表中的菜品
        for (Long id : ids) {
            dishMapper.deleteById(id);
            //删除菜品关联的口味数据
            dishFlavorMapper.deleteByDishId(id);
        }*/

        //优化

        //批量删除菜品表中的菜品
        dishMapper.deleteByIds(ids);

        //批量删除菜品关联的口味数据
        dishFlavorMapper.deleteByDishIds(ids);

    }

    /**
     * 根据id查询菜品
     * @param id
     * @return
     */
    public DishVO getByIdWithFlavor(Long id) {
        //根据id查询菜品数据
        Dish dish = dishMapper.getById(id);

        //根据菜品id查询口味数据
        List<DishFlavor> dishFlavors =dishFlavorMapper.getByDishId(id);

        //将两个数据封装到VO
        DishVO dishVO= new DishVO();
        BeanUtils.copyProperties(dish,dishVO);
        dishVO.setFlavors(dishFlavors);

        return dishVO;
    }

    /**
     * 根据id修改菜品基本信息和对应的口味信息
     * @param dishVO
     */
    public void updateWithFlavor(DishVO dishVO) {
        //创建实体类
        Dish dish=new Dish();
        //dishVO部分数据拷贝到dish
        BeanUtils.copyProperties(dishVO,dish);

        //根据id修改菜品基本数据
        dishMapper.update(dish);

        //删除所有对应的口味数据
        dishFlavorMapper.deleteByDishId(dishVO.getId());

        //获取口味数组并且给每个口味实体添加菜品id
        List<DishFlavor> flavors = dishVO.getFlavors();
        //可能会有新添加的口味，要重新给他们添加菜品id
        flavors.forEach(dishFlavor -> {
            dishFlavor.setDishId(dishVO.getId());
        });

        //口味不为空时
        if(flavors!=null && flavors.size()>0){
            //向口味插入n条数据
            dishFlavorMapper.insertBatch(flavors);
        }

    }

    /**
     * 菜品起售或停售
     * @param status
     * @param id
     */
    public void startOrStop(Integer status, Long id) {
        //增加代码复用性
        Dish dish = Dish.builder()
                .status(status)
                .id(id)
                .build();

        //调用持久层修改员工信息
        dishMapper.update(dish);

        if (status == StatusConstant.DISABLE) {
            // 如果是停售操作，还需要将包含当前菜品的套餐也停售
            List<Long> dishIds = new ArrayList<>();
            dishIds.add(id);
            // select setmeal_id from setmeal_dish where dish_id in (?,?,?)
            //根据菜品id获取套餐ids
            List<Long> setmealIds = setmealDishMapper.getSetmealIdsByDishIds(dishIds);
            if (setmealIds != null && setmealIds.size() > 0) {
                for (Long setmealId : setmealIds) {
                    Setmeal setmeal = Setmeal.builder()
                            .id(setmealId)
                            .status(StatusConstant.DISABLE)
                            .build();
                    //当前套餐也禁用
                    setmealMapper.update(setmeal);
                }
            }
        }
    }

    /**
     * 根据分类id查询菜品
     * @param categoryId
     * @return
     */
    public List<Dish> list(Long categoryId) {
        //要查询 可以售卖的菜品
        Dish dish=Dish.builder()
                .categoryId(categoryId)
                .status(StatusConstant.ENABLE)
                .build();

        return dishMapper.list(dish);
    }

}
