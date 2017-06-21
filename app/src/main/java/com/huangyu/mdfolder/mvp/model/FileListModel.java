package com.huangyu.mdfolder.mvp.model;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;

import com.huangyu.library.app.BaseApplication;
import com.huangyu.library.mvp.IBaseModel;
import com.huangyu.library.util.FileUtils;
import com.huangyu.mdfolder.bean.FileItem;
import com.huangyu.mdfolder.utils.DateUtils;
import com.huangyu.mdfolder.utils.SDCardUtils;
import com.huangyu.mdfolder.utils.ZipUtils;
import com.huangyu.mdfolder.utils.comparator.AlphabetComparator;
import com.huangyu.mdfolder.utils.comparator.TimeComparator;
import com.huangyu.mdfolder.utils.comparator.TypeComparator;
import com.huangyu.mdfolder.utils.filter.SearchFilter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Created by huangyu on 2017-5-24.
 */
public class FileListModel implements IBaseModel {

    public FileListModel() {

    }

    public List<File> getFileList(String path, String searchStr) {
        return FileUtils.listFilesInDirWithFilter(path, new SearchFilter(searchStr), false);
    }

//    public List<File> getAppsFileList(String searchStr) {
//        return FileUtils.listFilesInDirWithFilter(getStorageCardPath(), new ApkFilter(searchStr), true);
//    }
//
//    public List<File> getMusicFileList(String searchStr) {
//        return FileUtils.listFilesInDirWithFilter(getStorageCardPath(), new MusicFilter(searchStr), true);
//    }
//
//    public List<File> getPhotoFileList(String searchStr) {
//        return FileUtils.listFilesInDirWithFilter(getStorageCardPath(), new PhotoFilter(searchStr), true);
//    }
//
//    public List<File> getVideoFileList(String searchStr) {
//        return FileUtils.listFilesInDirWithFilter(getStorageCardPath(), new VideoFilter(searchStr), true);
//    }

    public List<FileItem> getDocumentList(String searchStr, ContentResolver contentResolver) {
        String[] projection = new String[]{MediaStore.Files.FileColumns.DATA,
                MediaStore.Files.FileColumns.TITLE, MediaStore.Files.FileColumns.SIZE,
                MediaStore.Files.FileColumns.DATE_MODIFIED};

        String selection = MediaStore.Files.FileColumns.MIME_TYPE + "= ? "
                + " or " + MediaStore.Files.FileColumns.MIME_TYPE + " = ? "
                + " or " + MediaStore.Files.FileColumns.MIME_TYPE + " = ? "
                + " or " + MediaStore.Files.FileColumns.MIME_TYPE + " = ? "
                + " or " + MediaStore.Files.FileColumns.MIME_TYPE + " = ? "
                + " or " + MediaStore.Files.FileColumns.MIME_TYPE + " = ? "
                + " or " + MediaStore.Files.FileColumns.MIME_TYPE + " = ? "
                + " or " + MediaStore.Files.FileColumns.MIME_TYPE + " = ? ";

        // 分别对应 doc pdf ppt xls wps docx pptx xlsx 类型的文档
        String[] selectionArgs = new String[]{
                "application/msword",
                "application/pdf",
                "application/vnd.ms-powerpoint",
                "application/vnd.ms-excel",
                "application/vnd.ms-works",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                "application/vnd.openxmlformats-officedocument.presentationml.presentation",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"};

        Cursor cursor = contentResolver.query(MediaStore.Files.getContentUri("external"), projection,
                selection, selectionArgs, MediaStore.Files.FileColumns.DATE_MODIFIED + " desc");

        if (cursor != null) {
            ArrayList<FileItem> documentList = new ArrayList<>();
            while (cursor.moveToNext()) {
                String fileName = cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.TITLE));
                String filePath = cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA));
                String date = cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.DATE_MODIFIED));

                if (FileUtils.isFileExists(filePath)) {
                    FileItem fileItem = new FileItem();
                    fileItem.setName(fileName);
                    fileItem.setPath(filePath);
                    fileItem.setSize(FileUtils.getFileSize(filePath));
                    fileItem.setDate(DateUtils.getFormatDate(Long.valueOf(date) * 1000));
                    fileItem.setParent(null);
                    fileItem.setIsDirectory(false);
                    fileItem.setIsPhoto(false);
                    if (TextUtils.isEmpty(searchStr) || fileName.contains(searchStr)) {
                        documentList.add(fileItem);
                    }
                }
            }
            cursor.close();
            return documentList;
        }
        return null;
    }

    public List<FileItem> getVideoList(String searchStr, ContentResolver contentResolver) {
        String[] projection = new String[]{MediaStore.Video.VideoColumns.DATA,
                MediaStore.Video.VideoColumns.DISPLAY_NAME, MediaStore.Video.VideoColumns.SIZE,
                MediaStore.Video.VideoColumns.DATE_MODIFIED};

        Cursor cursor = contentResolver.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                projection,
                null, null, MediaStore.Video.VideoColumns.DATE_MODIFIED + " desc");

        if (cursor != null) {
            ArrayList<FileItem> videoList = new ArrayList<>();
            while (cursor.moveToNext()) {
                String fileName = cursor.getString(cursor.getColumnIndex(MediaStore.Video.VideoColumns.DISPLAY_NAME));
                String filePath = cursor.getString(cursor.getColumnIndex(MediaStore.Video.VideoColumns.DATA));
                String date = cursor.getString(cursor.getColumnIndex(MediaStore.Video.VideoColumns.DATE_MODIFIED));

                if (FileUtils.isFileExists(filePath)) {
                    FileItem fileItem = new FileItem();
                    fileItem.setName(fileName);
                    fileItem.setPath(filePath);
                    fileItem.setSize(FileUtils.getFileSize(filePath));
                    fileItem.setDate(DateUtils.getFormatDate(Long.valueOf(date) * 1000));
                    fileItem.setParent(null);
                    fileItem.setIsDirectory(false);
                    fileItem.setIsPhoto(false);
                    if (TextUtils.isEmpty(searchStr) || fileName.contains(searchStr)) {
                        videoList.add(fileItem);
                    }
                }
            }
            cursor.close();
            return videoList;
        }
        return null;
    }

    public List<FileItem> getImageList(String searchStr, ContentResolver contentResolver) {
        Uri imageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String[] projection = new String[]{MediaStore.Images.ImageColumns.DATA,
                MediaStore.Images.ImageColumns.DISPLAY_NAME,
                MediaStore.Images.ImageColumns.SIZE,
                MediaStore.Images.ImageColumns.DATE_MODIFIED};
        Cursor cursor = contentResolver.query(imageUri, projection, null, null,
                MediaStore.Images.ImageColumns.DATE_MODIFIED + " desc");
        if (cursor != null) {
            ArrayList<FileItem> imageList = new ArrayList<>();
            while (cursor.moveToNext()) {
                String filePath = cursor.getString(cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA));
                String fileName = cursor.getString(cursor.getColumnIndex(MediaStore.Images.ImageColumns.DISPLAY_NAME));
                String date = cursor.getString(cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATE_MODIFIED));

                if (FileUtils.isFileExists(filePath)) {
                    FileItem fileItem = new FileItem();
                    fileItem.setName(fileName);
                    fileItem.setPath(filePath);
                    fileItem.setSize(FileUtils.getFileSize(filePath));
                    fileItem.setDate(DateUtils.getFormatDate(Long.valueOf(date) * 1000));
                    fileItem.setParent(null);
                    fileItem.setIsDirectory(false);
                    fileItem.setIsPhoto(true);
                    if (TextUtils.isEmpty(searchStr) || fileName.contains(searchStr)) {
                        imageList.add(fileItem);
                    }
                }
            }
            cursor.close();
            return imageList;
        }
        return null;
    }

    public List<FileItem> getAudioList(String searchStr, ContentResolver contentResolver) {
        String[] projection = new String[]{MediaStore.Audio.AudioColumns.DATA,
                MediaStore.Audio.AudioColumns.DISPLAY_NAME, MediaStore.Audio.AudioColumns.SIZE,
                MediaStore.Audio.AudioColumns.DATE_MODIFIED};

        Cursor cursor = contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection, null, null, MediaStore.Audio.AudioColumns.DATE_MODIFIED + " desc");

        if (cursor != null) {
            ArrayList<FileItem> audioList = new ArrayList<>();
            while (cursor.moveToNext()) {
                String fileName = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.DISPLAY_NAME));
                String filePath = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.DATA));
                String date = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.DATE_MODIFIED));

                if (FileUtils.isFileExists(filePath)) {
                    FileItem fileItem = new FileItem();
                    fileItem.setName(fileName);
                    fileItem.setPath(filePath);
                    fileItem.setSize(FileUtils.getFileSize(filePath));
                    fileItem.setDate(DateUtils.getFormatDate(Long.valueOf(date) * 1000));
                    fileItem.setParent(null);
                    fileItem.setIsDirectory(false);
                    fileItem.setIsPhoto(false);
                    if (TextUtils.isEmpty(searchStr) || fileName.contains(searchStr)) {
                        audioList.add(fileItem);
                    }
                }
            }
            cursor.close();
            return audioList;
        }
        return null;
    }

    /**
     * 获取根目录文件路径
     *
     * @return
     */
    public String getRootPath() {
        return Environment.getRootDirectory().getPath();
    }

    /**
     * 获取存储卡路径
     *
     * @return
     */
    public String getStorageCardPath(boolean isInner) {
        return SDCardUtils.getStoragePath(BaseApplication.getInstance().getApplicationContext(), isInner);
    }

    /**
     * 获取下载目录路径
     *
     * @return
     */
    public String getDownloadPath() {
        return SDCardUtils.getSDCardPath() + "Download";
    }

    /**
     * 按字母排序
     */
    public List<FileItem> orderByAlphabet(List<FileItem> fileList) {
        Collections.sort(fileList, new AlphabetComparator());
        return fileList;
    }

    /**
     * 按时间排序
     */
    public List<FileItem> orderByTime(List<FileItem> fileList) {
        Collections.sort(fileList, new TimeComparator());
        return fileList;
    }

    /**
     * 按类型排序
     */
    public List<FileItem> orderByType(List<FileItem> fileList) {
        Collections.sort(fileList, new TypeComparator());
        return fileList;
    }

    /**
     * 压缩文件
     *
     * @param resFiles    文件列表
     * @param zipFilePath 文件路径
     * @return true/false
     */
    public boolean zipFileList(Collection<File> resFiles, String zipFilePath) {
        try {
            return ZipUtils.zipFiles(resFiles, zipFilePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 解压缩文件
     *
     * @param resFiles    文件列表
     * @param zipFilePath 文件路径
     * @return true/false
     */
    public boolean unzipFileList(Collection<File> resFiles, String zipFilePath) {
        try {
            return ZipUtils.unzipFiles(resFiles, zipFilePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

}
