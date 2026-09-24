package com.campus.delivery.modules.delivery.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.delivery.modules.delivery.entity.Rider;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 骑手数据访问。主责：成员4。
 */
@Mapper
public interface RiderMapper extends BaseMapper<Rider> {

    /** 抢单成功：配送中订单数 +1 */
    @Update("UPDATE rider SET delivering_count = delivering_count + 1, update_time = NOW() "
            + "WHERE id = #{riderId} AND delivering_count < max_delivering")
    int increaseDelivering(@Param("riderId") String riderId);

    /** 完成配送：配送中订单数 -1，累计收入累加 */
    @Update("UPDATE rider SET delivering_count = GREATEST(delivering_count - 1, 0), "
            + "total_income = total_income + #{fee}, update_time = NOW() WHERE id = #{riderId}")
    int decreaseDelivering(@Param("riderId") String riderId, @Param("fee") java.math.BigDecimal fee);
}
