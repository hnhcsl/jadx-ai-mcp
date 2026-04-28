package com.zin.jadxaimcp.server.routes;

import io.javalin.http.Context;
import jadx.gui.ui.MainWindow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.swing.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.File;
import java.util.Map;
import com.zin.jadxaimcp.utils.JadxAIMCPPluginError;

public class ProjectRoutes {
    private static final Logger logger = LoggerFactory.getLogger(ProjectRoutes.class);
    private final MainWindow mainWindow;

    public ProjectRoutes(MainWindow mainWindow) {
        this.mainWindow = mainWindow;
    }

    /**
     * 处理打开 APK/文件的请求
     * GET /open-apk?path=/absolute/path/to/app.apk
     */
    public void handleOpenApk(Context ctx) {
        String filePath = ctx.queryParam("path");
        if (filePath == null || filePath.isEmpty()) {
            JadxAIMCPPluginError.handleError(ctx, 400, "Missing required parameter 'path'", logger);
            return;
        }

        try {
            File file = new File(filePath);
            if (!file.exists()) {
                JadxAIMCPPluginError.handleError(ctx, 404, "File not found: " + filePath, logger);
                return;
            }

            Path path = file.toPath();
            // GUI 操作必须在 Swing 事件分发线程 (EDT) 中执行
            SwingUtilities.invokeLater(() -> {
                try {
                    mainWindow.open(path);
                    logger.info("JADX AI MCP: Successfully triggered opening of {}", filePath);
                } catch (Exception e) {
                    logger.error("Failed to open file in JADX-GUI: " + e.getMessage(), e);
                }
            });

            ctx.json(Map.of("result", "Loading started for " + filePath));
        } catch (Exception e) {
            JadxAIMCPPluginError.handleError(ctx, "Internal error while opening file: " + e.getMessage(), e, logger);
        }
    }
}
