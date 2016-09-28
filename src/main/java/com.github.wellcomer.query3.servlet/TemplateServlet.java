package com.github.wellcomer.query3.servlet;

import com.github.wellcomer.query3.core.Query;
import com.github.wellcomer.query3.core.QueryList;
import com.github.wellcomer.query3.core.QueryStorage;
import com.github.wellcomer.query3.core.Template;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedHashMap;

/**
 * Created on 12.11.15.
 * num - номер заявки
 * t - имя шаблона
 * templatePath - путь к шаблонам ( в локальной ФС )
 * saveToPath - путь для сохранения файлов
 * redirectURLPrefix - префикс URL для скачивания файлов
 */

//@WebServlet("/template")
public class TemplateServlet extends HttpServlet {

    private Template template;
    private QueryList queryList;
    private String saveToPath;

    @Override
    public void init(ServletConfig servletConfig) {

        ServletContext servletContext = servletConfig.getServletContext();
        String servletRealPath = servletContext.getRealPath("/");

        String dbPath = servletContext.getInitParameter("dbPath");
        if (dbPath.equalsIgnoreCase("default"))
            dbPath = Paths.get(servletRealPath, ".db").toString();

        String dbName = servletContext.getInitParameter("dbName");

        String stgBackendName = servletContext.getInitParameter("stgBackendName");
        if (stgBackendName.equalsIgnoreCase("default"))
            stgBackendName = "mapdb";

        String characterEncoding = servletContext.getInitParameter("characterEncoding");
        if (characterEncoding.equalsIgnoreCase("default"))
            characterEncoding = Charset.defaultCharset().toString();

        String templatePath = servletConfig.getInitParameter("templatePath");
        if (templatePath.equalsIgnoreCase("default"))
            templatePath = Paths.get(servletRealPath, "template").toString();

        saveToPath = servletConfig.getInitParameter("saveToPath");
        if (saveToPath.equalsIgnoreCase("default"))
            saveToPath = System.getProperty("java.io.tmpdir");

        template = new Template(templatePath);
        queryList = QueryListSingle.getInstance(stgBackendName, dbPath, dbName, characterEncoding);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        Integer queryNumber = Integer.decode(req.getParameter("num"));
        String templateName = req.getParameter("t");

        Query query;

        try {
            query = queryList.getStorageBackend().get(queryNumber);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        String fileName = template.fillAndSave(query, templateName, saveToPath);

        res.setContentType("application/vnd.oasis.opendocument.spreadsheet");
        res.setHeader("Content-Disposition", "filename=\"" + fileName + "\"");

        FileInputStream in = new FileInputStream(Paths.get(saveToPath, fileName).toString());
        OutputStream out = res.getOutputStream();

        byte[] buffer = new byte[4096];
        int length;
        while ((length = in.read(buffer)) > 0){
            out.write(buffer, 0, length);
        }
        in.close();
        out.flush();

        Files.deleteIfExists(Paths.get(saveToPath, fileName));
    }
}


