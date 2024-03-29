package com.project.interfaces.web.service;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.project.interfaces.web.dto.StoredFileDTO;
import com.project.exception.FileStorageException;
import com.project.exception.MyFileNotFoundException;
//import com.project.model.entity.Progress;
import com.project.model.entity.FileStorageProperties;
//import com.project.model.repository.ProgressRepository;

@Service
public class FileStorageService {
	
    private final Path fileStorageLocation;
	private static final int BUFFER = 2048;
		
    @Autowired
    public FileStorageService(FileStorageProperties fileStorageProperties) {
    	
        this.fileStorageLocation = Paths.get(fileStorageProperties.getUploadDir())
                .toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new FileStorageException("Could not create the directory where the uploaded files will be stored.", ex);
        }
    }
    
    public String zipB64(List<File> files) throws IOException {
	    byte[] buffer = new byte[1024];
	    ByteArrayOutputStream baos = new ByteArrayOutputStream(); 
	    try (ZipOutputStream zos = new ZipOutputStream(baos)) {
	        for (File f : files) {
	            try (FileInputStream fis = new FileInputStream(f)) {
	                zos.putNextEntry(new ZipEntry(f.getName()));
	                int length;
	                while ((length = fis.read(buffer)) > 0) {
	                    zos.write(buffer, 0, length);
	                }
	                zos.closeEntry();
	            }
	        }
	    }
	    byte[] bytes = baos.toByteArray();
	    return Base64.getEncoder().encodeToString(bytes);
    }

	public String zipB64(List<File> files, File rootFolder) throws IOException {
		URI base = rootFolder.toURI();
		byte[] buffer = new byte[1024];
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try (ZipOutputStream zos = new ZipOutputStream(baos)) {
			for (File f : files) {
				try (FileInputStream fis = new FileInputStream(f)) {
					String name = base.relativize(f.toURI()).getPath();
					zos.putNextEntry(new ZipEntry(name));
					int length;
					while ((length = fis.read(buffer)) > 0) {
						zos.write(buffer, 0, length);
					}
					zos.closeEntry();
				}
			}
		}
		byte[] bytes = baos.toByteArray();
		return Base64.getEncoder().encodeToString(bytes);
	}
    
    public StoredFileDTO storeFile(MultipartFile[] files, String formattedDate, String key) {
    	
        for(MultipartFile file : files) {
        	// Normalize file name
	    	String fileName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
	
	        try {
	            // Check if the file's name contains invalid characters
	            if(fileName.contains("..")) {
	                throw new FileStorageException("Sorry! Filename contains invalid path sequence " + fileName);
	            }
	
	            // Copy file to the target location (Replacing existing file with the same name)
	            String newFileName = formattedDate + File.separator + fileName;
	            Path targetLocation = this.fileStorageLocation.resolve(newFileName);
	            String path = targetLocation.toString().replace(fileName, "");
	            this.verifyPath(path);
	            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
	        }
	        catch (IOException ex) {
	            throw new FileStorageException("Could not store file " + fileName + ". Please try again!", ex);
	        }
	        
	        //process the file
        }
            return new StoredFileDTO(null);
        
    }

    public Resource loadFileAsResource(String fileName) {
        try {
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if(resource.exists()) {
                return resource;
            } else {
                throw new MyFileNotFoundException("File not found " + fileName);
            }
        } catch (MalformedURLException ex) {
            throw new MyFileNotFoundException("File not found " + fileName, ex);
        }
    }
    

	public List<String> unzip(File file) {
    	List<String> fileNames = new ArrayList<>();
    	try {
	
			BufferedOutputStream dest;
			FileInputStream fis = new FileInputStream(file);
			
			ZipInputStream zis = new ZipInputStream(
					new BufferedInputStream(fis));

			ZipEntry entry;
			
			String extension = FilenameUtils.getExtension(file.getName());
			String folder = file.getName().replace("."+extension, "");
			
			while ((entry = zis.getNextEntry()) != null) {
				if(!FilenameUtils.getExtension(entry.toString()).equals("")) {
				int count;
				byte[] data = new byte[BUFFER];
				// Cria os arquivos no disco
				String path = file.getPath();
				path = path.replace(file.getName(),"");
				String fileName = path + folder + File.separator + entry.getName();
				String location = fileName.replace(FilenameUtils.getName(fileName), "");
				this.verifyPath(location);
				FileOutputStream fos = new FileOutputStream(fileName);
				fileNames.add(fileName);
				
				dest = new BufferedOutputStream(fos, BUFFER);
				while ((count = zis.read(data, 0, BUFFER)) != -1) {
					dest.write(data, 0, count);
					
				}
				dest.flush();
				dest.close();
			}
			}
			zis.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return fileNames;
	}
    
    public static String formattedDate() {
		// data/hora atual
		LocalDateTime now = LocalDateTime.now();

		// formatar a data
		DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/uuuu");
		String formattedDate = dateFormatter.format(now);
		formattedDate = formattedDate.replace('/', '-');

		// formatar a hora
		DateTimeFormatter hourFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
		String formattedHour = hourFormatter.format(now);
		formattedHour = formattedHour.replace(':', '.');
		return formattedDate + " " + formattedHour;
	}
    
    public void verifyPath(String path) {
    	File file = new File(path);
		if(!file.exists()) file.mkdirs();
    }
    
}