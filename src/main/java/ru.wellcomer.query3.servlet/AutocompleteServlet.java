package ru.wellcomer.query3.servlet;

import ru.wellcomer.query3.core.Autocomplete;
import ru.wellcomer.query3.core.Json;
import ru.wellcomer.query3.core.QueryList;

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
import java.util.List;

/**
 * Created on 12.11.15.
 * dbPath - путь к БД заявок
 * autocompletePath - путь к файлам автокомплита
 * autoLearn - автоматическое обучение (сканирование всех полей в заявках и формирование файлов)
 * scanModifiedOnly - сканировать только измененные с последнего запуска заявки
 * mergePrevious - слияние файлов автокомплита с полями отсканированных заявок
 * maxItems - максимальное количество выдаваемых результатов в поле автокомплита
 */

//@WebServlet("/autocomplete")
public class AutocompleteServlet extends HttpServlet {

    private Autocomplete autocomp;
    private String dbPath, characterEncoding, autocompletePath;
    private boolean autoLearn, scanModifiedOnly, mergePrevious, disabled;
    private QueryList queryList;
    int maxItems;

    @Override
    public void init(ServletConfig servletConfig) {

        disabled = Boolean.parseBoolean(servletConfig.getInitParameter("disabled"));

        if (disabled)  // Автодополнение выключено
            return;

        ServletContext servletContext = servletConfig.getServletContext();
        String servletRealPath = servletContext.getRealPath("/");

        dbPath = servletContext.getInitParameter("dbPath");
        if (dbPath.equalsIgnoreCase("default"))
            dbPath = Paths.get(servletRealPath, ".db").toString();

        characterEncoding = servletContext.getInitParameter("characterEncoding");
        if (characterEncoding.equalsIgnoreCase("default"))
            characterEncoding = Charset.defaultCharset().toString();

        autocompletePath = servletConfig.getInitParameter("autocompletePath");
        if (autocompletePath.equalsIgnoreCase("default"))
            autocompletePath = Paths.get(servletRealPath, ".autocomplete").toString();

        autoLearn = Boolean.parseBoolean(servletConfig.getInitParameter("autoLearn"));
        scanModifiedOnly = Boolean.parseBoolean(servletConfig.getInitParameter("scanModifiedOnly"));
        mergePrevious = Boolean.parseBoolean(servletConfig.getInitParameter("mergePrevious"));

        try {
            maxItems = Integer.parseInt(servletConfig.getInitParameter("maxItems"));
        }
        catch (NumberFormatException e){
            maxItems = 0;
        }

        autocomp = new Autocomplete(autocompletePath, characterEncoding, maxItems);

        if (autoLearn) {
            queryList = new QueryList(dbPath, characterEncoding);
            try {
                autocomp.autolearn(queryList, scanModifiedOnly, mergePrevious);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        if (disabled)
            return;

        // онлайн обучение на новых заявках без перезапуска системы
        if (req.getParameter("update") != null){
            if (autoLearn)
                autocomp.autolearn(queryList, true, true);
            return;
        }

        List<String> lines;

        if (characterEncoding.equalsIgnoreCase("UTF-8"))
            lines = autocomp.get(req.getParameter ("f"), req.getParameter("term"));
        else // не UTF-8 системы
            lines = autocomp.get(req.getParameter ("f"), new String (req.getParameter("term").getBytes("ISO-8859-1"), "UTF-8"));

        res.setCharacterEncoding("UTF-8");
        res.setContentType("application/json");

        PrintWriter out = res.getWriter();
        out.print(Json.toString(lines));
    }

}
