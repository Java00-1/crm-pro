package com.bjpowernode.crm.workbench.controller;

import com.bjpowernode.crm.commons.constant.Constant;
import com.bjpowernode.crm.commons.domain.ReturnObject;
import com.bjpowernode.crm.commons.utils.DateUtils;
import com.bjpowernode.crm.commons.utils.SystemUtils;
import com.bjpowernode.crm.commons.utils.UUIDUtils;
import com.bjpowernode.crm.settings.domain.User;
import com.bjpowernode.crm.workbench.domain.ActivityRemark;
import com.bjpowernode.crm.workbench.service.ActivityRemarkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;
import java.util.Date;

@Controller
@RequestMapping("/workbench/activity")
public class ActivityRemarkController {

    @Autowired
    private ActivityRemarkService activityRemarkService;

    @RequestMapping("/saveCreateActivityRemark.do")
    @ResponseBody
    public Object saveCreateActivityRemark(String noteContent, String activityId, HttpSession session){
        ReturnObject returnObject = new ReturnObject();
        //进行参数的封装 activityRemark
        ActivityRemark activityRemark = new ActivityRemark();

        activityRemark.setId(UUIDUtils.getUUID());
        activityRemark.setNoteContent(noteContent);
        //获取当前用户
        User user = (User) session.getAttribute(Constant.USER_SESSION);
        activityRemark.setCreateBy(user.getId());
        activityRemark.setCreateTime(DateUtils.formatDateTime(new Date()));
        activityRemark.setEditFlag(Constant.REMARK_FLAG_ETERNAL);
        activityRemark.setActivityId(activityId);

        //封装完毕 进行方法调用
        try {
            int ret = activityRemarkService.saveCreateActivityRemark(activityRemark);

            if (ret > 0) {
                returnObject.setCode(Constant.RETURN_OBJECT_CODE_SUCCESS);
                returnObject.setRetData(activityRemark);
                returnObject.setMessage("保存"+SystemUtils.SYSTEM_SUCCESS);
            }else{
                returnObject.setCode(Constant.RETURN_OBJECT_CODE_FAIL);
                returnObject.setMessage(SystemUtils.SYSTEM_ERR0R);
            }
        }catch (Exception e){
            e.printStackTrace();
            returnObject.setCode(Constant.RETURN_OBJECT_CODE_FAIL);
            returnObject.setMessage(SystemUtils.SYSTEM_ERR0R);
        }

        return returnObject;
    }

    @RequestMapping("/deleteActivityRemarkById.do")
    @ResponseBody
    public Object deleteActivityRemarkById(String id){
        ReturnObject returnObject = new ReturnObject();
        //调用方法
        try{
            int ret = activityRemarkService.deleteActivityRemarkById(id);
            if (ret > 0) {
                returnObject.setCode(Constant.RETURN_OBJECT_CODE_SUCCESS);
                returnObject.setMessage("删除"+SystemUtils.SYSTEM_SUCCESS);
            }else{
                returnObject.setCode(Constant.RETURN_OBJECT_CODE_FAIL);
                returnObject.setMessage(SystemUtils.SYSTEM_ERR0R);
            }
        }catch (Exception e){
            e.printStackTrace();
            returnObject.setCode(Constant.RETURN_OBJECT_CODE_FAIL);
            returnObject.setMessage(SystemUtils.SYSTEM_ERR0R);
        }

        return returnObject;
    }

    @RequestMapping("/saveEditActivityRemark.do")
    @ResponseBody
    public Object saveEditActivityRemark(String id,String noteContent,ActivityRemark activityRemark,HttpSession session){
        ReturnObject returnObject = new ReturnObject();
        User user = (User) session.getAttribute(Constant.USER_SESSION);
        //封装参数
        activityRemark.setId(id);
        activityRemark.setNoteContent(noteContent);
        activityRemark.setEditBy(user.getId());
        activityRemark.setEditTime(DateUtils.formatDateTime(new Date()));
        activityRemark.setEditFlag(Constant.REMARK_FLAG_CHANGED);

        try {
            int ret = activityRemarkService.saveEidtActivityRemark(activityRemark);
            if (ret > 0) {
                returnObject.setCode(Constant.RETURN_OBJECT_CODE_SUCCESS);
                returnObject.setRetData(activityRemark);
                returnObject.setMessage("修改"+SystemUtils.SYSTEM_SUCCESS);
            }else{
                returnObject.setCode(Constant.RETURN_OBJECT_CODE_FAIL);
                returnObject.setMessage(SystemUtils.SYSTEM_ERR0R);
            }
        } catch (Exception e) {
            e.printStackTrace();
            returnObject.setCode(Constant.RETURN_OBJECT_CODE_FAIL);
            returnObject.setMessage(SystemUtils.SYSTEM_ERR0R);
        }
        return returnObject;
    }
}
