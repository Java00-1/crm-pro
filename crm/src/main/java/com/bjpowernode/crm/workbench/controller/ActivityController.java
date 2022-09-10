package com.bjpowernode.crm.workbench.controller;

import com.bjpowernode.crm.commons.constant.Constant;
import com.bjpowernode.crm.commons.domain.ReturnObject;
import com.bjpowernode.crm.commons.utils.DateUtils;
import com.bjpowernode.crm.commons.utils.HSSFUtils;
import com.bjpowernode.crm.commons.utils.SystemUtils;
import com.bjpowernode.crm.commons.utils.UUIDUtils;
import com.bjpowernode.crm.settings.domain.User;
import com.bjpowernode.crm.settings.service.UserService;
import com.bjpowernode.crm.workbench.domain.Activity;
import com.bjpowernode.crm.workbench.domain.ActivityRemark;
import com.bjpowernode.crm.workbench.service.ActivityRemarkService;
import com.bjpowernode.crm.workbench.service.ActivityService;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

@Controller
public class ActivityController {

    @Autowired
    private UserService userService;

    @Autowired
    private ActivityService activityService;

    @Autowired
    private ActivityRemarkService activityRemarkService;

    @RequestMapping("/workbench/activity/index.do")
    public String index(HttpServletRequest request){
        //查询数据
        List<User> userList = userService.queryAllUsers();

        //将数据封装到request作用域中
        request.setAttribute("userList",userList);

        return "workbench/activity/index";
    }

    @RequestMapping("/workbench/activity/saveCreateActivity.do")
    @ResponseBody
    public Object saveCreateActivity(Activity activity, HttpSession session){
        //获取当前登录对象
        User user = (User) session.getAttribute(Constant.USER_SESSION);
        //封装参数
        activity.setId(UUIDUtils.getUUID());
        activity.setCreateBy(user.getId());
        activity.setCreateTime(DateUtils.formatDateTime(new Date()));
        //调用service方法保存创建的市场活动
        ReturnObject returnObject = new ReturnObject();
        try {
            int ret = activityService.saveCreateActivity(activity);
            if (ret > 0){
                returnObject.setCode(Constant.RETURN_OBJECT_CODE_SUCCESS);
            }else {
                returnObject.setCode(Constant.RETURN_OBJECT_CODE_FAIL);
                returnObject.setMessage("系统忙,稍后再试...");
            }
        }catch (Exception e){
            e.printStackTrace();
            returnObject.setCode(Constant.RETURN_OBJECT_CODE_FAIL);
            returnObject.setMessage("系统忙,稍后再试...");
        }

        return returnObject;
    }

    @RequestMapping("/workbench/activity/queryActivityByConditionForPage.do")
    @ResponseBody
    public Object queryActivityByConditionForPage(String name,String owner,String startDate,String endDate,int pageNo,int pageSize){
        HashMap<String, Object> map = new HashMap<>();
        //封装参数
        map.put("name",name);
        map.put("owner",owner);
        map.put("startDate",startDate);
        map.put("endDate",endDate);
        map.put("beginNo",(pageNo-1)*pageSize);
        map.put("pageSize",pageSize);

        //调用方法 获取到查询的activity对象
        List<Activity> activityList = activityService.queryActivityByConditionForPage(map);
        //获取市场活动记录数
        int ret = activityService.queryCountOfActivityByCondition(map);
        HashMap<String, Object> retMap = new HashMap<>();
        //封装数据用于前端
        retMap.put("activityList",activityList);
        retMap.put("totalRows",ret);
        return retMap;
    }

    @RequestMapping("/workbench/activity/deleteActivityByIds.do")
    @ResponseBody
    public Object deleteActivityByIds(String[] id){
        ReturnObject returnObject = new ReturnObject();
        try {
            //调用方法
            int ret = activityService.deleteActivityByIds(id);
            if (ret > 0 ){//删除成功
                returnObject.setCode(Constant.RETURN_OBJECT_CODE_SUCCESS);
            }else {
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

    @RequestMapping("/workbench/activity/queryActivityById.do")
    public @ResponseBody Object queryActivityById(String id){
        //调用service层方法，查询市场活动
        Activity activity=activityService.queryActivityById(id);
        //根据查询结果，返回响应信息
        return activity;
    }

    @RequestMapping("/workbench/activity/saveEditActivityById.do")
    @ResponseBody
    public Object saveEditActivityById(Activity activity,HttpSession session){

        //获取当前用户信息
        User user = (User) session.getAttribute(Constant.USER_SESSION);

        activity.setEditBy(user.getId());
        activity.setEditTime(DateUtils.formatDateTime(new Date()));

        //调用方法进行查询
        ReturnObject returnObject = new ReturnObject();

        try {
            int ret = activityService.saveEditActivity(activity);
            if (ret > 0 ){//更新成功
                returnObject.setCode(Constant.RETURN_OBJECT_CODE_SUCCESS);
            }else {
                returnObject.setCode(Constant.RETURN_OBJECT_CODE_FAIL);
                returnObject.setMessage(SystemUtils.SYSTEM_ERR0R);
            }
        }catch (Exception e){
            returnObject.setCode(Constant.RETURN_OBJECT_CODE_FAIL);
            returnObject.setMessage(SystemUtils.SYSTEM_ERR0R);
        }

        return returnObject;
    }


    @RequestMapping("/workbench/activity/exportAllActivities.do")
    public void exportAllActivities(HttpServletResponse response) throws Exception{


        //调用方法查询市场活动
        List<Activity> activityList = activityService.queryAllActivities();
        //创建相应的对象进行excel文件的生成
        //生成文件
        HSSFWorkbook wb = new HSSFWorkbook();
        //生成页
        HSSFSheet sheet = wb.createSheet("市场活动列表");
        //生成第一行
        HSSFRow row = sheet.createRow(0);
        //生成第一列
        HSSFCell cell = row.createCell(0);
        cell.setCellValue("ID");
        cell = row.createCell(1);
        cell.setCellValue("所有者");
        cell = row.createCell(2);
        cell.setCellValue("姓名");
        cell = row.createCell(3);
        cell.setCellValue("开始日期");
        cell = row.createCell(4);
        cell.setCellValue("结束日期");
        cell = row.createCell(5);
        cell.setCellValue("成本");
        cell = row.createCell(6);
        cell.setCellValue("描述");
        cell = row.createCell(7);
        cell.setCellValue("创建时间");
        cell = row.createCell(8);
        cell.setCellValue("创建者");
        cell = row.createCell(9);
        cell.setCellValue("修改时间");
        cell = row.createCell(10);
        cell.setCellValue("修改人");

        //遍历activityList进行数据的赋值
        if (activityList != null && activityList.size() > 0){
            Activity activity = null;
            for (int i=0;i<activityList.size();i++){
                activity = activityList.get(i);
                row = sheet.createRow(i + 1);

                cell = row.createCell(0);
                cell.setCellValue(activity.getId());
                cell = row.createCell(1);
                cell.setCellValue(activity.getOwner());
                cell = row.createCell(2);
                cell.setCellValue(activity.getName());
                cell = row.createCell(3);
                cell.setCellValue(activity.getStartDate());
                cell = row.createCell(4);
                cell.setCellValue(activity.getEndDate());
                cell = row.createCell(5);
                cell.setCellValue(activity.getCost());
                cell = row.createCell(6);
                cell.setCellValue(activity.getDescription());
                cell = row.createCell(7);
                cell.setCellValue(activity.getCreateTime());
                cell = row.createCell(8);
                cell.setCellValue(activity.getCreateBy());
                cell = row.createCell(9);
                cell.setCellValue(activity.getEditTime());
                cell = row.createCell(10);
                cell.setCellValue(activity.getEditBy());
            }
        }

       /* //使用流创建excel文件 写到硬盘
        OutputStream outputStream = new FileOutputStream("E:\\DailyWork\\crm-file\\activityList.xls");
        //调用插件实现java对象转换为excel文件
        wb.write(outputStream);
        //关闭流
        outputStream.close();
        wb.close();
*/

        //将excel文件下载到访问端
        //设置响应头
        //浏览器接收到响应信息之后，默认情况下，直接在显示窗口中打开响应信息；
        //即使打不开，也会调用应用程序打开；只有实在打不开，才会激活文件下载窗口。
        //可以设置响应头信息，使浏览器接收到响应信息之后，直接激活文件下载窗口，即使能打开也不打开
        response.setContentType("application/octet-stream;charset=UTF-8");
        response.addHeader("Content-Disposition","attachment;filename=activityList.xls");
        //管道
        OutputStream out = response.getOutputStream();
        //读取文件
        /*InputStream inputStream = new FileInputStream("E:\\DailyWork\\crm-file\\activityList.xls");
        //直接弹出下载窗口
        int len = 0;
        byte[] bytes = new byte[256];
        //读取文件
        while ((len=inputStream.read(bytes))!=-1){
            out.write(bytes,0,len);
        }

        inputStream.close();*/

        //直接在内存中进行数据的展示即可 无需进行内存 ---> 硬盘 ---> 内存的操作 大大提高程序访问速度
        wb.write(out);
        wb.close();
        out.flush();

    }

    @RequestMapping("/workbench/activity/importActivityFile.do")
    @ResponseBody
    //json数据进行返回
    public Object importActivityFile(MultipartFile activityFile,HttpSession session){
        User user = (User) session.getAttribute(Constant.USER_SESSION);
        List<Activity> activityList = new ArrayList<>();
        ReturnObject returnObject = new ReturnObject();
        try {
           /* //把excel文件写入到磁盘中
            String filename = activityFile.getOriginalFilename();
            //新建文件
            File file = new File("E:\\DailyWork\\crm-file\\", filename);
            activityFile.transferTo(file);
            //解析excel文件，获取文件中的数据，并且封装成activityList
            //根据excel文件生成HSSFWorkbook对象，封装了excel文件的所有信息
            InputStream inputStream = new FileInputStream("E:\\DailyWork\\crm-file\\"+filename);*/

            //优化
            InputStream inputStream = activityFile.getInputStream();

            //poi 文件 调用方法进行解析
            HSSFWorkbook wb = new HSSFWorkbook(inputStream);
            //获取页数 改页保存了所有的信息
            HSSFSheet sheet = wb.getSheetAt(0);
            HSSFRow row = null;
            HSSFCell cell = null;
            Activity activity = null;
            //循环获取行数 第一行的数据不需要封装到activity中
            for (int i=1;i<=sheet.getLastRowNum();i++){
                row = sheet.getRow(i);
                //获取列数
                activity = new Activity();
                activity.setId(UUIDUtils.getUUID());
                //折中方案 使用当前用户的id作为owner 用户无需关注
                activity.setOwner(user.getId());
                activity.setCreateTime(DateUtils.formatDateTime(new Date()));
                activity.setCreateBy(user.getId());

                for (int j=0;j<row.getLastCellNum();j++){//循环次数为列数
                    //进行数据的封装
                    cell = row.getCell(j);
                    String ret = HSSFUtils.getCellValue(cell);
                    if (j==0){
                        activity.setName(ret);
                    }else if (j==1){
                        activity.setStartDate(ret);
                    }else if (j==2){
                        activity.setEndDate(ret);
                    }else if (j==3){
                        activity.setCost(ret);
                    }else if (j==4){
                        activity.setDescription(ret);
                    }
                }
                //将数据添加到List中
                activityList.add(activity);
            }

            //调用方法
            int ret = activityService.saveCreateActivityByList(activityList);
            if (ret > 0 ){
                returnObject.setRetData(ret);
                returnObject.setCode(Constant.RETURN_OBJECT_CODE_SUCCESS);
            }else{
                returnObject.setCode(Constant.RETURN_OBJECT_CODE_FAIL);
                //returnObject.setRetData(ret);
                returnObject.setMessage(SystemUtils.SYSTEM_ERR0R);
            }

        } catch (Exception e) {
            e.printStackTrace();
            returnObject.setCode(Constant.RETURN_OBJECT_CODE_FAIL);
            returnObject.setMessage(SystemUtils.SYSTEM_ERR0R);
        }

        return returnObject;
    }

    @RequestMapping("/workbench/activity/showActivityDetailById.do")
    public String showActivityDetailById(String id,HttpServletRequest request){
        //调用方法获取activity
        Activity activity = activityService.queryActivityForDetailById(id);
        //get remarkList
        List<ActivityRemark> remarkList = activityRemarkService.queryActivityRemarkForDetailByActivityId(id);

        //进行数据封装 将数据保存在request作用域中
        request.setAttribute("activity",activity);
        request.setAttribute("remarkList",remarkList);

        //进行请求转发 视图解析器
        return "workbench/activity/detail";
    }
}
