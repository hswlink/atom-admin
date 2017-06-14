package com.atom.mvc.controller;

import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.atom.constants.MsgCode;
import com.atom.mvc.controller.base.BaseController;
import com.atom.mvc.http.Request;
import com.atom.mvc.http.Response;
import com.atom.mvc.http.ResponseHeader;
import com.atom.mvc.model.Syslog;
import com.atom.mvc.service.base.IService;
import com.atom.tools.DateTimeKit;
import com.atom.tools.FileInfo;
import com.atom.tools.FileUploderKit;
import com.jfinal.core.ApplicationContextKit;
import com.jfinal.kit.HttpKit;
import com.jfinal.kit.StrKit;
import com.jfinal.upload.UploadFile;


public class CommonController extends BaseController {
	
	public static final Logger logger = Logger.getLogger(CommonController.class);
	public void index() throws Exception {

		Date date = new Date();
		String content = getPara("content");
//		String jsonpCallback = getPara("jsonpCallback");
		content = StringUtils.trim(content);
		String method = getRequest().getMethod().toUpperCase();
		// 解密报文

		Response response = new Response();
		ResponseHeader header = new ResponseHeader();

		Request r = new Request();
		header.setTrdate(DateTimeKit.format(new Date()));
		
		

		// 请求为空
		if (StringUtils.isBlank(content)) {
			header.setErrormsg(MsgCode.Code_1001);
			header.setSuccflag(Response.FAIL);
			response.setHeader(header);
			renderJson(response);
			System.out.println("response:"+JSON.toJSONString(response));
//			renderText(jsonpCallback+"("+JSON.toJSONString(response)+")");
			return;
		}

		// 检查结果
		try {
			r = JSON.parseObject(content, Request.class);

			
		} catch (Exception e) {
			// 请求格式错误
			header.setTrcode("FMSXXXXXXX");
			header.setErrormsg("请求格式错误");
			header.setSuccflag(Response.FAIL);
			response.setHeader(header);
			System.out.println("response:"+JSON.toJSONString(response));
			renderJson(response);
//			renderText(jsonpCallback+"("+JSON.toJSONString(response)+")");
			return;
		}
		header.setTrcode(r.getHeader().getTrcode());
		header.setAppseq(r.getHeader().getAppseq());
		
		String ip = getRequest().getServerName();
		if (StrKit.isBlank(ip)) {
			ip = "127.0.0.1";
		}
		r.getHeader().setIp(ip);

		IService service = null;
		try {
			 String trcode = r.getHeader().getTrcode();
			 service = ApplicationContextKit.getBean(trcode);
		} catch (Exception e) {
			e.printStackTrace();
			header.setErrorcode(MsgCode.Code_1003);// 未找到服务号
			header.setSuccflag(Response.FAIL);
			response.setHeader(header);
			renderJson(response);
			System.out.println("response:"+JSON.toJSONString(response));
//			renderText(jsonpCallback+"("+JSON.toJSONString(response)+")");
			return;
		}
		try {
			response = service.excute(r, response, header, method);
			System.out.println("response:"+JSON.toJSONString(response));
		} catch (Exception e) {
			e.printStackTrace();
			header.setErrorcode(MsgCode.Code_500);// 服务器错误
			header.setSuccflag(Response.FAIL);
			response.setHeader(header);
			renderJson(response);
//			renderText(jsonpCallback+"("+JSON.toJSONString(response)+")");
			return;			
		}
		
//		renderText(jsonpCallback+"("+JsonKit.toJson(response)+")");
		renderJson(response);
		Date over = new Date();
		long execute_time = over.getTime() - date.getTime();
		logger.debug(r.getHeader().getTrcode() + " execute time is :" + execute_time + " ms");

		Syslog syslog = new Syslog();
		
		
		
		return;
	}
	


	
	public void fileUpload(){
		UploadFile up = getFile();
		FileInfo fileInfo =FileUploderKit.upload(up);
		renderJson(fileInfo);
	}

	public static void main(String args[]){
		JSONObject param = new JSONObject();
		JSONObject action_info = new JSONObject();
		JSONObject scene = new JSONObject();
		scene.put("scene_id", 123);
		action_info.put("scene", scene);
		
		param.put("expire_seconds",604800 );
		param.put("action_name", "QR_SCENE");
		param.put("action_info", action_info);
		String url = "https://api.weixin.qq.com/cgi-bin/qrcode/create?access_token=81TsvvMJ2_2uaH6yF-CVPgqUNqeq1A8gU2biEf_biJCCZ-WQpwu1O_UMlGOJ9RIYZl348cwqIhlMSYSDESbkyAK50YwULpeEgK4KOQ1IeM0";
		String res = HttpKit.post(url, JSON.toJSONString(param));
		System.out.println(res);
	}
	

	

}
