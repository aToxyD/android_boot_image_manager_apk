package dz.atoxyd.ABM.util;

//original author: atoxyd 
//modified by: ........

import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;



public class UnzipUtility implements Constants {
    private static final int BUFFER_SIZE = 4096;

    public void unzipall(String zipFilePath, String destDirectory) throws IOException {
        File destDir = new File(destDirectory);
        if (!destDir.exists()) {destDir.mkdir();}

        ZipInputStream zipIn = new ZipInputStream(new FileInputStream(zipFilePath));
        ZipEntry entry = zipIn.getNextEntry();
        while (entry != null) {
            if (!entry.isDirectory()) {
                File dir=new File(destDirectory,entry.getName());
                if(!dir.getParentFile().exists())
                    dir.getParentFile().mkdirs();
                extractFile(zipIn, destDirectory+"/"+entry.getName());
            }

            zipIn.closeEntry();
            entry = zipIn.getNextEntry();
        }
        zipIn.close();
    }
    public void unzipfile(String zipFilePath, String destDirectory,String[] f) throws IOException {
        File destDir = new File(destDirectory);
        if (!destDir.exists()) {destDir.mkdir();}

        ZipInputStream zipIn = new ZipInputStream(new FileInputStream(zipFilePath));
        ZipEntry entry = zipIn.getNextEntry();
        while (entry != null) {
            //if (!entry.isDirectory() && entry.getName().contains(f)) {
            if (!entry.isDirectory() && inlist(entry.getName(),f)) {

                File dir=new File(destDirectory,entry.getName());
                if(!dir.getParentFile().exists())
                    dir.getParentFile().mkdirs();
                extractFile(zipIn, destDirectory+"/"+entry.getName());
                zipIn.closeEntry();
                //break;
            }
            zipIn.closeEntry();
            entry = zipIn.getNextEntry();
        }
        zipIn.close();
    }
    private boolean inlist(String e,String[] f){
        boolean flag=false;
        for (String a : f) {
            if (e.contains(a)) {
                flag= true;
                break;
            }
        }
        return flag;
    }
    private void extractFile(ZipInputStream zipIn, String filePath) throws IOException {
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath));
        byte[] bytesIn = new byte[BUFFER_SIZE];
        int read = 0;
        while ((read = zipIn.read(bytesIn)) != -1) {
            bos.write(bytesIn, 0, read);
        }
        bos.close();
    }

    public Boolean testZip(String zipFilePath,String tip) throws IOException {
        ZipInputStream zipIn = new ZipInputStream(new FileInputStream(zipFilePath));
        ZipEntry entry = zipIn.getNextEntry();
        boolean gasit=false;
        while (entry != null) {
            if (!entry.isDirectory()) {
				if(tip.equalsIgnoreCase("kernel")||tip.equalsIgnoreCase("unpackramdisk")){
					if(entry.getName().equalsIgnoreCase("boot.img")){
						gasit=true;
						break;
					}
				}
				else if(tip.equalsIgnoreCase("recovery")){
					if(entry.getName().equalsIgnoreCase("recovery.img")){
						gasit=true;
						break;
					}  
				}
            }
            zipIn.closeEntry();
            entry = zipIn.getNextEntry();
        }
        zipIn.close();
        return gasit;
    }
}
