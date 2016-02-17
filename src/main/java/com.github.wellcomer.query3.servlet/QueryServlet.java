package com.github.wellcomer.query3.servlet;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.HashMap;

import com.github.wellcomer.query3.core.Json;
import com.github.wellcomer.query3.core.Query;
import com.github.wellcomer.query3.core.QueryList;

/**
 * Created on 10.11.15.
 * f=
 * get_nnum - получение номера новой заявки
 * load_query - загрузка заявки
 * save_query - сохранение заявки
 * get_query_count - количество заявок
 * search - поиск заявки по заданным полям
 */

//@WebServlet("/query")
public class QueryServlet extends HttpServlet {

    private String dbPath, characterEncoding;
    private QueryList queryList;

    @Override
    public void init(ServletConfig servletConfig) throws ServletException{

        ServletContext servletContext = servletConfig.getServletContext();
        String servletRealPath = servletContext.getRealPath("/");

        dbPath = servletContext.getInitParameter("dbPath");
        if (dbPath.equalsIgnoreCase("default"))
            dbPath = Paths.get(servletRealPath, ".db").toString();

        characterEncoding = servletContext.getInitParameter("characterEncoding");
        if (characterEncoding.equalsIgnoreCase("default"))
            characterEncoding = Charset.defaultCharset().toString();

        queryList = new QueryList(dbPath, characterEncoding);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");

        res.setCharacterEncoding("UTF-8");
        res.setContentType("application/json");

        PrintWriter out = res.getWriter();

        String funcName = req.getParameter ("f");
        if (funcName.equals(""))
            return;

        switch (funcName){
            case "get_nnum":
                try {
                    out.print(queryList.getNewNumber().toString());
                }
                catch (IOException e){
                    out.print("1");
                }
                break;
            case "load_query":
                Integer queryNumber = Integer.decode(req.getParameter("n"));
                out.print(Json.toString(queryList.get(queryNumber)));
                break;
            case "save_query":
                HashMap<String, String> saveResult = new HashMap<>();
                try {
                    queryNumber = Integer.decode(req.getParameter("n"));

                    Query query = new Query();
                    Enumeration<String> paramNames = req.getParameterNames();

                    while (paramNames.hasMoreElements()) {
                        String paramName = paramNames.nextElement();
                        query.put(paramName, req.getParameter(paramName));
                    }

                    queryList.add(queryNumber, query);
                    saveResult.put("status", "OK");
                }
                catch (Exception e){
                    e.printStackTrace();
                    saveResult.put("status", "ERR1");
                }
                out.print(Json.toString(saveResult));
                break;
            case "get_query_count":
                out.print(queryList.size().toString());
                break;
            case "search":
                Query query = new Query();
                Enumeration<String> paramNames = req.getParameterNames();

                while (paramNames.hasMoreElements()) {
                    String paramName = paramNames.nextElement();
                    if (paramName.equals("f") || paramName.equals("n") || paramName.equals(""))
                        continue;
                    query.put(paramName, req.getParameter(paramName));
                }
                out.print(Json.toString(queryList.find(query)));
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        doGet(req, resp);
    }
}
