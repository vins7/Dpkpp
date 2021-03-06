package com.app.service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import javax.persistence.RollbackException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.app.dao.AbsentDao;
import com.app.helper.Constants;
import com.app.helper.SessionHelper;
import com.app.model.Absent;
import com.app.model.Lpp;
import com.app.model.Person;
import com.app.model.UserActivity;
import com.app.pojo.PojoAbsentPerson;
import com.app.pojo.PojoHistoriAbsen;
import com.app.pojo.PojoPagination;

@Service
public class AbsentSevice extends BaseService {

	@Autowired
	private AbsentDao absentDao;
	
	
	@Autowired
	private UserActivityService UserActivity;

	@Value("${path.absen.masuk}")
    private String pathMasuk;
	
	@Value("${path.absen.keluar}")
    private String pathKeluar;
	
	
	@Value("${path.photo}")
	private String path;

	@Value("${photo.not.found}")
	private String photoNotFound;
	
	public Absent getById(String id) throws Exception{
		Absent absent = absentDao.getById(id);
		if(absent != null) {
			return absent;
		}else {
			throw new Exception("Absent not exist !");
		}
	}
	
	public void valIdExist(Absent absent) throws Exception{
		if(absentDao.getById(absent.getId()) == null){
			throw new Exception("Absent not exist !");
		}
	}
	
	public PojoPagination getAbsentByPaging(int page,int limit) throws Exception{
		PojoPagination pojo = new PojoPagination();
		pojo.setData(absentDao.getAbsentByPaging(page,limit,null));
		pojo.setCount(absentDao.getCountAbsentByPaging(null));
		return pojo;
	}
	
	public PojoPagination getAbsentBySearch(int page,int limit,String inquiry) throws Exception{
		PojoPagination pojo = new PojoPagination();
		pojo.setData(absentDao.getAbsentByPaging(page,limit,inquiry));
		pojo.setCount(absentDao.getCountAbsentByPaging(inquiry));
		return pojo;
	}
	
	public LinkedHashMap<String, Integer> getUserAbsentById() throws Exception{
		return absentDao.getUserAbsentById(SessionHelper.getPerson().getId());
	}
	
	public LinkedHashMap<String, Integer> getUserAbsentByIdAdmin(String id) throws Exception{
		return absentDao.getUserAbsentById(id);
	}
	
	
	public PojoAbsentPerson checkAbsentByPersonId(String id) throws Exception{
		return absentDao.checkAbsentByPersonId(id);
	}
	
	public String absentIn(MultipartFile file,String pojoAbsent) throws Exception{
		Absent absent = new Absent();
		PojoAbsentPerson absents = super.readValue(pojoAbsent, PojoAbsentPerson.class);
		try {
			PojoAbsentPerson pojoAbsents = checkAbsentByPersonId(SessionHelper.getPerson().getId());
			if(pojoAbsents!=null) {
				return "You has been absent today !";
			}
			if (file == null) {
				throw new Exception("Photo cannot be null !");
			}
			
			String fileName = file.getOriginalFilename();
			InputStream is = file.getInputStream();
			absent.setTypeFileAbsenIn(file.getContentType());
			absent.setFileNameAbsenIn(fileName);
			absent.setDateIn(new java.sql.Timestamp(new Date().getTime()));
			absent.setPerson(SessionHelper.getPerson());
			absent.setLangtitudeAbsenIn(absents.getLangtitude());
			absent.setLongtitudeAbsenIn(absents.getLongtitude());
			absent.setLocationAbsenIn(absents.getLocation());
			absent.setCreatedBy(SessionHelper.getPerson().getName());
			absentDao.save(absent);
			
			if (file != null) {
				Files.copy(is, Paths.get(pathMasuk + absent.getId() + "_" + absent.getPerson().getName() + "_"
						+ Constants.ABSENT_IN + "_" + fileName), StandardCopyOption.REPLACE_EXISTING);
			}
			UserActivity act = new UserActivity();
			act.setUser(SessionHelper.getUser());
			act.setType(Constants.USER_ABSENT_IN);
			act.setDate(new Date());
			UserActivity.save(act);
			return "Absent entry success !";
		} catch (Exception e) {
			throw e;
		}
	}
	
	public String absentOut(MultipartFile file,String pojoAbsent) throws Exception{
		PojoAbsentPerson absents = super.readValue(pojoAbsent, PojoAbsentPerson.class);
		try {
			PojoAbsentPerson pojoAbsents = checkAbsentByPersonId(SessionHelper.getPerson().getId());
			if(pojoAbsents==null) {
				return "You have to be absent first !";
			}
			if (file == null) {
				throw new Exception("Photo cannot be null !");
			}
			Absent absent = absentDao.getAbsentById(pojoAbsents.getId());
			if (absent == null) {
				return "Absent not found !";
			}

			if (absent.getDateOut() == null) {
				String fileName = file.getOriginalFilename();
				InputStream is = file.getInputStream();
				absent.setTypeFileAbsenOut(file.getContentType());
				absent.setFileNameAbsenOut(fileName);
				absent.setDateOut(new java.sql.Timestamp(new Date().getTime()));
				absent.setLangtitudeAbsenOut(absents.getLangtitude());
				absent.setLongtitudeAbsenOut(absents.getLongtitude());
				absent.setLocationAbsenOut(absents.getLocation());
				absent.setUpdatedBy(SessionHelper.getPerson().getName());
				absentDao.edit(absent);
				if (file != null) {
					Files.copy(is, Paths.get(pathKeluar + absent.getId() + "_" + absent.getPerson().getName() + "_"
							+ Constants.ABSENT_OUT + "_" + fileName), StandardCopyOption.REPLACE_EXISTING);
				}
				UserActivity act = new UserActivity();
				act.setUser(SessionHelper.getUser());
				act.setType(Constants.USER_ABSENT_OUT);
				act.setDate(new Date());
				UserActivity.save(act);
				return "Absent out success !";
			}
			
			return "you've been absent today "; 
			
		} catch (Exception e) {
			throw e;
		}
	}
	
	public PojoPagination getAbsensiHistori(Integer page,Integer limit) throws Exception {
		PojoPagination data = new PojoPagination();
		List<PojoHistoriAbsen> dataHistory = new ArrayList<PojoHistoriAbsen>();
		List<Object[]> dataList = absentDao.getHistoriAbsentUSer(SessionHelper.getPerson().getId(), page, limit,null,null,null);
		data.setCount(absentDao.getCountAbsentPerson(SessionHelper.getPerson().getId(),null,null,null));
		for(Object[] dat:dataList) {
			PojoHistoriAbsen abs = new PojoHistoriAbsen();
			abs.setId((String)dat[0]);
			abs.setDateIn((String)dat[1]);
			abs.setDateOut((String)dat[2]);
			abs.setLocationIn((String)dat[3]);
			abs.setLocationOut((String)dat[4]);
			abs.setStatus((String)dat[5]);
			abs.setLatIn((String)dat[6]);
			abs.setLatOut((String)dat[7]);
			abs.setLongIn((String)dat[8]);
			abs.setLongOut((String)dat[9]);
			abs.setFile_nameIn((String)dat[10]);
			abs.setFile_nameOut((String)dat[11]);
			abs.setNama((String)dat[12]);
			abs.setNip((String)dat[13]);
			abs.setStatusAbsen((String)dat[14]);
			dataHistory.add(abs);
		}
		data.setData(dataHistory);
		
		
		
		return data;
	}
	
	public PojoPagination getAbsensiHistoriBySearch(Integer page,Integer limit,String inquiry,Date periodBegin,Date periodEnd) throws Exception {
		PojoPagination data = new PojoPagination();
		List<PojoHistoriAbsen> dataHistory = new ArrayList<PojoHistoriAbsen>();
		List<Object[]> dataList = absentDao.getHistoriAbsentUSer(SessionHelper.getPerson().getId(), page, limit,inquiry,periodBegin,periodEnd);
		data.setCount(absentDao.getCountAbsentPerson(SessionHelper.getPerson().getId(),inquiry,periodBegin,periodEnd));
		for(Object[] dat:dataList) {
			PojoHistoriAbsen abs = new PojoHistoriAbsen();
			abs.setId((String)dat[0]);
			abs.setDateIn((String)dat[1]);
			abs.setDateOut((String)dat[2]);
			abs.setLocationIn((String)dat[3]);
			abs.setLocationOut((String)dat[4]);
			abs.setStatus((String)dat[5]);
			abs.setLatIn((String)dat[6]);
			abs.setLatOut((String)dat[7]);
			abs.setLongIn((String)dat[8]);
			abs.setLongOut((String)dat[9]);
			abs.setFile_nameIn((String)dat[10]);
			abs.setFile_nameOut((String)dat[11]);
			abs.setNama((String)dat[12]);
			abs.setNip((String)dat[13]);
			abs.setStatusAbsen((String)dat[14]);
			dataHistory.add(abs);
		}
		data.setData(dataHistory);
		
		
		
		return data;
	}
	
	public PojoPagination getAdminAbsensiHistori(String id,Integer page,Integer limit) throws Exception {
		PojoPagination data = new PojoPagination();
		List<PojoHistoriAbsen> dataHistory = new ArrayList<PojoHistoriAbsen>();
		List<Object[]> dataList = absentDao.getHistoriAbsentUSer(id, page, limit,null,null,null);
		data.setCount(absentDao.getCountAbsentPerson(id,null,null,null));
		for(Object[] dat:dataList) {
			PojoHistoriAbsen abs = new PojoHistoriAbsen();
			abs.setId((String)dat[0]);
			abs.setDateIn((String)dat[1]);
			abs.setDateOut((String)dat[2]);
			abs.setLocationIn((String)dat[3]);
			abs.setLocationOut((String)dat[4]);
			abs.setStatus((String)dat[5]);
			abs.setLatIn((String)dat[6]);
			abs.setLatOut((String)dat[7]);
			abs.setLongIn((String)dat[8]);
			abs.setLongOut((String)dat[9]);
			abs.setFile_nameIn((String)dat[10]);
			abs.setFile_nameOut((String)dat[11]);
			abs.setNama((String)dat[12]);
			abs.setNip((String)dat[13]);
			abs.setStatusAbsen((String)dat[14]);
			dataHistory.add(abs);
		}
		data.setData(dataHistory);
		
		
		
		return data;
	}
	
	public PojoPagination getAdminAbsensiHistoriBySearch(String id,Integer page,Integer limit,String inquiry,Date periodBegin,Date periodEnd) throws Exception {
		PojoPagination data = new PojoPagination();
		List<PojoHistoriAbsen> dataHistory = new ArrayList<PojoHistoriAbsen>();
		List<Object[]> dataList = absentDao.getHistoriAbsentUSer(id, page, limit,inquiry,periodBegin,periodEnd);
		data.setCount(absentDao.getCountAbsentPerson(id,inquiry,periodBegin,periodEnd));
		for(Object[] dat:dataList) {
			PojoHistoriAbsen abs = new PojoHistoriAbsen();
			abs.setId((String)dat[0]);
			abs.setDateIn((String)dat[1]);
			abs.setDateOut((String)dat[2]);
			abs.setLocationIn((String)dat[3]);
			abs.setLocationOut((String)dat[4]);
			abs.setStatus((String)dat[5]);
			abs.setLatIn((String)dat[6]);
			abs.setLatOut((String)dat[7]);
			abs.setLongIn((String)dat[8]);
			abs.setLongOut((String)dat[9]);
			abs.setFile_nameIn((String)dat[10]);
			abs.setFile_nameOut((String)dat[11]);
			abs.setNama((String)dat[12]);
			abs.setNip((String)dat[13]);
			abs.setStatusAbsen((String)dat[14]);
			dataHistory.add(abs);
		}
		data.setData(dataHistory);
		
		
		
		return data;
	}
	
	public HashMap<String, Object> getPhotoAbsent(String Id) throws Exception{
		Absent input = absentDao.getAbsentById(Id);
		System.out.println("admin "+input.getDateIn());
		HashMap<String, Object> rtData = new HashMap<String, Object>();
		String filePathIn =input.getId()+"_"+input.getPerson().getName()+"_"+Constants.ABSENT_IN+"_"+input.getFileNameAbsenIn();
		String photoIn;
		File fileIn;
		fileIn = new File(pathMasuk + filePathIn);
		if (fileIn.exists()) {
			try {
				photoIn = Base64.getEncoder().encodeToString(FileUtils.readFileToByteArray(fileIn));
				rtData.put("photoIn", photoIn);
				System.out.println("ext in"+input.getTypeFileAbsenIn());
				rtData.put("extIn",input.getTypeFileAbsenIn());
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			try {
				fileIn = new File(path + photoNotFound);
				System.out.println("ext in"+input.getTypeFileAbsenIn());
				photoIn = Base64.getEncoder().encodeToString(FileUtils.readFileToByteArray(fileIn));
				rtData.put("photoIn", photoIn);
				rtData.put("extIn", "image/"+FilenameUtils.getExtension(fileIn.toString()));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		String filePathOut = input.getId()+"_"+input.getPerson().getName()+"_"+Constants.ABSENT_OUT+"_"+input.getFileNameAbsenOut();
		String photoOut;
		File fileOut;
		fileOut = new File(pathKeluar + filePathOut);
		if (fileOut.exists()) {
			try {
				photoOut = Base64.getEncoder().encodeToString(FileUtils.readFileToByteArray(fileOut));
				rtData.put("photoOut", photoOut);
				rtData.put("extOut", input.getTypeFileAbsenOut());
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			try {
				fileOut = new File(path + photoNotFound);
				photoOut = Base64.getEncoder().encodeToString(FileUtils.readFileToByteArray(fileOut));
				rtData.put("photoOut", photoOut);
				rtData.put("extOut", "image/"+FilenameUtils.getExtension(fileOut.toString()));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		rtData.put("detail",input);


		return rtData;
	
	}
	
	public void delete(Absent absent) throws Exception,RollbackException	 {	
		try {
			valIdExist(absent);

			absentDao.delete(absent);
		} catch (Exception e) {
			throw e;
		}
	}
	
	public void adminAbsentIn(Absent absents) throws Exception{
		try {
			Absent absent = absentDao.getAbsentByPersonId(absents.getPerson().getId());
			if (absent.getId() == null) {
				absents.setStatus("APPROVE");
				absents.setDateOut(new Date());
				absents.setDateIn(new Date());
				absents.setLovAbsent(absents.getLovAbsent());
				absents.setCreatedBy(SessionHelper.getPerson().getName());
				absentDao.save(absents);	
			} else {
				absent.setStatus("APPROVE");
				absent.setLovAbsent(absents.getLovAbsent());
				absents.setUpdatedBy(SessionHelper.getPerson().getName());
				absentDao.edit(absent);
			}
		}catch (Exception e) {
			throw e;
		}
	}	
	
	public void adminAbsentIn(List<Absent> listAbsent) throws Exception{
		for(Absent absent : listAbsent) {
			adminAbsentIn(absent);
		}
	}
	
	public List<Absent> getByPersonId(String id) throws Exception{
		return absentDao.getByPersonId(id);
	}
}
