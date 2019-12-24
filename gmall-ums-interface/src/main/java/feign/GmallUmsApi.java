package feign;


import com.atguigu.core.bean.Resp;
import entity.MemberEntity;
import entity.MemberReceiveAddressEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

public interface GmallUmsApi {

    /**
     * 根据用户名和密码查询用户
     * @param username
     * @param password
     * @return
     */
    @GetMapping("ums/member/query")
    public Resp<MemberEntity> queryUser(
            @RequestParam("username") String username,
            @RequestParam("password") String password
    );


    /**
     * 根据id查询用户信息
     * @param id
     * @return
     */
    @GetMapping("ums/member/info/{id}")
    public Resp<MemberEntity> queryMemberById(@PathVariable("id") Long id);


    /**
     * 根据用户id查询收货地址
     * @param userId
     * @return
     */
    @GetMapping("ums/memberreceiveaddress/{userId}")
    public Resp<List<MemberReceiveAddressEntity>> queryAddressesByUserId(@PathVariable("userId")Long userId);
}



