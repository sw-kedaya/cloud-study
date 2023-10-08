package cn.itcast.hotel.listener;

import cn.itcast.hotel.constant.MQConstant;
import cn.itcast.hotel.service.IHotelService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MQListener {

    @Autowired
    private IHotelService hotelService;

    @RabbitListener(queues = MQConstant.HOTEL_INSERT_QUEUE)
    public void insertListener(String id) {
        hotelService.insertHotelById(id);
    }

    @RabbitListener(queues = MQConstant.HOTEL_DELETE_QUEUE)
    public void deleteListener(String id) {
        hotelService.deleteHotelById(id);
    }
}
