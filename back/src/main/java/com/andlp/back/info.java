package com.andlp.back;

/**
 * 717219917@qq.com      2017/11/27  17:52
 */

public class info {
//实现非侵入式侧滑    不用继承第三方baseActivity  不实现其他多余接口      application中初始化
// 主要功能    维护fragment和子fragment的栈    +侧滑
//0.一个单例（SwipeBack）  维护多个activity>>list<bean_activity>
//1.每个activity 维护自身一个fragment和多个子fragment （list）+子fragment的栈 （list）
// 2.每个fragment 维护自身一个fragment和多个子fragment （list）+子fragment的栈 （list）
//3.子fragment继续维护自身fragment和下级子fragment.....好像二叉树
//4.



}
