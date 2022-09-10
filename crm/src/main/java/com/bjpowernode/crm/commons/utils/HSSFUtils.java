package com.bjpowernode.crm.commons.utils;

import org.apache.poi.hssf.usermodel.HSSFCell;

public class HSSFUtils {
    public static String getCellValue(HSSFCell cell){
        String ret="";
        /*if (cell == null ){
            return ret;
        }*/
        //根据不同列的数据类型将数据进行分类
        if(cell.getCellType()==HSSFCell.CELL_TYPE_STRING){//String
            ret=cell.getStringCellValue();
        }else if(cell.getCellType()==HSSFCell.CELL_TYPE_NUMERIC){//double
            ret=cell.getNumericCellValue()+"";
        }else if(cell.getCellType()==HSSFCell.CELL_TYPE_BOOLEAN){//boolean
            ret=cell.getBooleanCellValue()+"";
        }else if(cell.getCellType()==HSSFCell.CELL_TYPE_FORMULA){//formula
            ret=cell.getCellFormula();
        }else{
            ret="";
        }

        return ret;
    }
}
