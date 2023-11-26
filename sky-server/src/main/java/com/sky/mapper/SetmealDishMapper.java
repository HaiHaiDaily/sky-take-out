package com.sky.mapper;

import com.sky.entity.SetmealDish;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SetmealDishMapper {
    /**
     * 根据菜品id查询对应的套餐id
     * @param dishIds
     * @return
     */
    List<Long> getSetmealIdsByDishIds(List<Long> dishIds);

    /**
     *批量保存套餐和菜品的关联关系
     * @param setmealDishes
     */
    void insertBatch(List<SetmealDish> setmealDishes);

    /**
     * 根据 套餐ids 批量删除 套餐和菜品的关联数据
     * @param ids
     */
    void deleteBatchBySetmealIds(List<Long> ids);

    /**
     * 根据套餐id查询 套餐和菜品的关联数据
     * @param setmealId
     * @return
     */
    @Select("select * from setmeal_dish where setmeal_id = #{setmealId}")
    List<SetmealDish> getBySetmealId(Long setmealId);

    /**
     * 根据套餐id删除 套餐和菜品的关联数据
     * @param setmealId
     */
    @Delete("delete from setmeal_dish where setmeal_id = #{setmealId}")
    void deleteBatchBySetmealId(Long setmealId);
}
