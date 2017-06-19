package com.huangyu.mdfolder.mvp.model;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import com.huangyu.library.mvp.IBaseModel;
import com.huangyu.library.util.FileUtils;
import com.huangyu.mdfolder.bean.FileItem;
import com.huangyu.mdfolder.utils.DateUtils;
import com.huangyu.mdfolder.utils.SDCardUtils;
import com.huangyu.mdfolder.utils.comparator.AlphabetComparator;
import com.huangyu.mdfolder.utils.comparator.TimeComparator;
import com.huangyu.mdfolder.utils.comparator.TypeComparator;
import com.huangyu.mdfolder.utils.filter.ApkFilter;
import com.huangyu.mdfolder.utils.filter.MusicFilter;
import com.huangyu.mdfolder.utils.filter.PhotoFilter;
import com.huangyu.mdfolder.utils.filter.SearchFilter;
import com.huangyu.mdfolder.utils.filter.VideoFilter;

import java.io.File;
import java.util.ArrayList;
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

    public List<File> getAppsFileList(String searchStr) {
        return FileUtils.listFilesInDirWithFilter(getSDCardPath(), new ApkFilter(searchStr), true);
    }

    public List<File> getMusicFileList(String searchStr) {
        return FileUtils.listFilesInDirWithFilter(getSDCardPath(), new MusicFilter(searchStr), true);
    }

    public List<File> getPhotoFileList(String searchStr) {
        return FileUtils.listFilesInDirWithFilter(getSDCardPath(), new PhotoFilter(searchStr), true);
    }

    public List<File> getVideoFileList(String searchStr) {
        return FileUtils.listFilesInDirWithFilter(getSDCardPath(), new VideoFilter(searchStr), true);
    }

    public List<FileItem> getDocumentList(ContentResolver contentResolver) {
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
                + " or " + MediaStore.Files.FileColumns.MIME_TYPE + " = ? "
                + " or " + MediaStore.Files.FileColumns.MIME_TYPE + " = ? ";

        // 分别对应 txt doc pdf ppt xls wps docx pptx xlsx 类型的文档
        String[] selectionArgs = new String[]{
                "text/plain",
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
            ArrayList<FileItem> mDocuments = new ArrayList<>();
            while (cursor.moveToNext()) {
                String fileName = cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.TITLE));
                String filePath = cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA));
//                String size = cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.SIZE));
                String date = cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.DATE_MODIFIED));

                if (FileUtils.isFileExists(filePath)) {
                    FileItem fileItem = new FileItem();
                    fileItem.setName(fileName);
                    fileItem.setPath(filePath);
                    fileItem.setSize(FileUtils.getFileSize(filePath));
                    fileItem.setDate(DateUtils.getFormatDate(Long.valueOf(date) * 1000));
                    fileItem.setParent(null);
                    fileItem.setIsDirectory(false);
                    mDocuments.add(fileItem);
                }
            }
            cursor.close();
            return mDocuments;
        }
        return null;
    }

    public List<FileItem> getVideoList(ContentResolver contentResolver) {
        String[] projection = new String[]{MediaStore.Video.VideoColumns.DATA,
                MediaStore.Video.VideoColumns.DISPLAY_NAME, MediaStore.Video.VideoColumns.SIZE,
                MediaStore.Video.VideoColumns.DATE_MODIFIED};

        Cursor cursor = contentResolver.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                projection,
                null, null, MediaStore.Video.VideoColumns.DATE_MODIFIED + " desc");

        if (cursor != null) {
            ArrayList<FileItem> mVideos = new ArrayList<>();
            while (cursor.moveToNext()) {
                String fileName = cursor.getString(cursor.getColumnIndex(MediaStore.Video.VideoColumns.DISPLAY_NAME));
                String filePath = cursor.getString(cursor.getColumnIndex(MediaStore.Video.VideoColumns.DATA));
//                String size = cursor.getString(cursor.getColumnIndex(MediaStore.Video.VideoColumns.SIZE));
                String date = cursor.getString(cursor.getColumnIndex(MediaStore.Video.VideoColumns.DATE_MODIFIED));

                if (FileUtils.isFileExists(filePath)) {
                    FileItem fileItem = new FileItem();
                    fileItem.setName(fileName);
                    fileItem.setPath(filePath);
                    fileItem.setSize(FileUtils.getFileSize(filePath));
                    fileItem.setDate(DateUtils.getFormatDate(Long.valueOf(date) * 1000));
                    fileItem.setParent(null);
                    fileItem.setIsDirectory(false);
                    mVideos.add(fileItem);
                }
            }
            cursor.close();
            return mVideos;
        }
        return null;
    }

    public List<FileItem> getImageList(ContentResolver contentResolver) {
        Uri imageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String[] projection = new String[]{MediaStore.Images.ImageColumns.DATA,
                MediaStore.Images.ImageColumns.DISPLAY_NAME,
                MediaStore.Images.ImageColumns.SIZE,
                MediaStore.Images.ImageColumns.DATE_MODIFIED};
        Cursor cursor = contentResolver.query(imageUri, projection, null, null,
                MediaStore.Images.ImageColumns.DATE_MODIFIED + " desc");
        if (cursor != null) {
            ArrayList<FileItem> mImages = new ArrayList<>();
            while (cursor.moveToNext()) {
                String filePath = cursor.getString(cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA));
                String fileName = cursor.getString(cursor.getColumnIndex(MediaStore.Images.ImageColumns.DISPLAY_NAME));
//                String size = cursor.getString(cursor.getColumnIndex(MediaStore.Images.ImageColumns.SIZE));
                String date = cursor.getString(cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATE_MODIFIED));

                if (FileUtils.isFileExists(filePath)) {
                    FileItem fileItem = new FileItem();
                    fileItem.setName(fileName);
                    fileItem.setPath(filePath);
                    fileItem.setSize(FileUtils.getFileSize(filePath));
                    fileItem.setDate(DateUtils.getFormatDate(Long.valueOf(date) * 1000));
                    fileItem.setParent(null);
                    fileItem.setIsDirectory(false);
                    mImages.add(fileItem);
                }
            }
            cursor.close();
            return mImages;
        }
        return null;
    }

    public List<FileItem> getAudioList(ContentResolver contentResolver) {
        String[] projection = new String[]{MediaStore.Audio.AudioColumns.DATA,
                MediaStore.Audio.AudioColumns.DISPLAY_NAME, MediaStore.Audio.AudioColumns.SIZE,
                MediaStore.Audio.AudioColumns.DATE_MODIFIED};

        Cursor cursor = contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection, null, null, MediaStore.Audio.AudioColumns.DATE_MODIFIED + " desc");

        if (cursor != null) {
            ArrayList<FileItem> mAudios = new ArrayList<>();
            while (cursor.moveToNext()) {
                String fileName = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.DISPLAY_NAME));
                String filePath = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.DATA));
//                String size = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.SIZE));
                String date = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.DATE_MODIFIED));

                if (FileUtils.isFileExists(filePath)) {
                    FileItem fileItem = new FileItem();
                    fileItem.setName(fileName);
                    fileItem.setPath(filePath);
                    fileItem.setSize(FileUtils.getFileSize(filePath));
                    fileItem.setDate(DateUtils.getFormatDate(Long.valueOf(date) * 1000));
                    fileItem.setParent(null);
                    fileItem.setIsDirectory(false);
                    mAudios.add(fileItem);
                }
            }
            cursor.close();
            return mAudios;
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
     * 获取sd卡目录路径
     *
     * @return
     */
    public String getSDCardPath() {
        return SDCardUtils.getSDCardPath();
    }

    /**
     * 获取sd卡目录路径
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

}
