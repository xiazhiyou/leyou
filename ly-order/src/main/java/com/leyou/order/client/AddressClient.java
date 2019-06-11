package com.leyou.order.client;

import com.leyou.order.dto.AddressDTO;

import java.util.ArrayList;
import java.util.List;

public abstract class AddressClient {
    public static final List<AddressDTO> addressList = new ArrayList<AddressDTO>(){
        {
            AddressDTO address = new AddressDTO();
            address.setId(1L);
            address.setAddress("太白南路");
            address.setCity("西安");
            address.setDistrict("雁塔区");
            address.setName("mushrooom");
            address.setPhone("186****7292");
            address.setState("陕西");
            address.setZipCode("7100710");
            address.setIsDefault(true);
            add(address);

            AddressDTO address2 = new AddressDTO();
            address2.setId(2L);
            address2.setAddress("盛世商都");
            address2.setCity("西安");
            address2.setDistrict("长安区");
            address2.setName("mushroom");
            address2.setPhone("186****7292");
            address2.setState("陕西");
            address2.setZipCode("03500150");
            address2.setIsDefault(false);
            add(address2);
        }
    };

    public static AddressDTO findById(Long id){
        for (AddressDTO addressDTO : addressList) {
            if(addressDTO.getId() == id){
                return addressDTO;
            }
        }
        return null;
    }
}