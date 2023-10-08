package cn.itcast.hotel.service;

import cn.itcast.hotel.pojo.Hotel;
import cn.itcast.hotel.pojo.PageResult;
import cn.itcast.hotel.pojo.RequestPrams;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

public interface IHotelService extends IService<Hotel> {
    PageResult selectPage(RequestPrams requestPrams);

    Map<String, List<String>> filters(RequestPrams requestPrams);

    List<String> suggestion(String prefix);

    void insertHotelById(String id);

    void deleteHotelById(String id);
}
