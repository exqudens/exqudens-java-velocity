package exqudens.java.velocity;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.context.Context;
import org.apache.velocity.tools.ToolManager;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.stream.Collectors;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@AllArgsConstructor
@Getter(AccessLevel.PROTECTED)
@Slf4j
public class Service implements Runnable {

    String contextString;
    File templateFile;
    File outputFile;
    List<String> toolStringList;
    String charsetString;

    @Override
    @SneakyThrows
    public void run() {
        var toolManager = new ToolManager(true);
        var velocityContext = toolManager.createContext();

        if (toolStringList != null && !toolStringList.isEmpty()) {
            installGenericTools(toolStringList, velocityContext);
        }

        if (contextString != null) {
            String propertiesContent = Collections
                    .list(new StringTokenizer(contextString, ";"))
                    .stream()
                    .map(token -> ((String) token).trim())
                    .collect(Collectors.joining("\n"));
            var properties = new Properties();
            try (InputStream is = new ByteArrayInputStream(propertiesContent.getBytes(StandardCharsets.UTF_8))) {
                properties.load(is);
            }
            Map<Object, Object> propertiesMap = new HashMap<>(properties);
            Map<String, Object> map = propertiesMap
                    .entrySet()
                    .stream()
                    .map(entry -> Map.entry(entry.getKey().toString(), entry.getValue()))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            log.info("Using {} as context", map);
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                velocityContext.put(entry.getKey(), entry.getValue());
            }
        }

        var charset = Charset.forName(charsetString != null ? charsetString : StandardCharsets.UTF_8.name());
        var config = new Properties();

        log.info("Using {} as input and default encoding", charset);
        config.setProperty(Velocity.ENCODING_DEFAULT, charset.toString());
        config.setProperty(Velocity.INPUT_ENCODING, charset.toString());
        config.setProperty(Velocity.RESOURCE_LOADERS, "file");

        if (templateFile.getParentFile() != null) {
            config.setProperty(Velocity.FILE_RESOURCE_LOADER_PATH, templateFile.getParentFile().getAbsolutePath());
        }

        var engine = new VelocityEngine();
        engine.init(config);

        try (
                OutputStreamWriter osw = new OutputStreamWriter(outputFile == null ? System.out : new FileOutputStream(outputFile), charset.newEncoder());
                Writer writer = new BufferedWriter(osw)
        ) {
            log.info("Merging template {} to {}", templateFile, outputFile == null ? "stdout" : outputFile);
            engine.mergeTemplate(templateFile.getName(), charset.name(), velocityContext, writer);
        }
    }

    @SneakyThrows
    private void installGenericTools(final List<String> tools, final Context velocityContext) {
        for (String tool : tools) {
            if ("class".equals(tool)) {
                velocityContext.put("class", new org.apache.velocity.tools.generic.ClassTool());
            } else if ("collection".equals(tool)) {
                velocityContext.put("collection", new org.apache.velocity.tools.generic.CollectionTool());
            } else if ("context".equals(tool)) {
                velocityContext.put("context", new org.apache.velocity.tools.generic.ContextTool());
            } else if ("date".equals(tool)) {
                velocityContext.put("date", new org.apache.velocity.tools.generic.ComparisonDateTool());
            } else if ("display".equals(tool)) {
                velocityContext.put("display", new org.apache.velocity.tools.generic.DisplayTool());
            } else if ("esc".equals(tool)) {
                velocityContext.put("esc", new org.apache.velocity.tools.generic.EscapeTool());
            } else if ("field".equals(tool)) {
                velocityContext.put("field", new org.apache.velocity.tools.generic.FieldTool());
            } else if ("json".equals(tool)) {
                velocityContext.put("json", new org.apache.velocity.tools.generic.JsonTool());
            } else if ("link".equals(tool)) {
                velocityContext.put("link", new org.apache.velocity.tools.generic.LinkTool());
            } else if ("log".equals(tool)) {
                velocityContext.put("log", new org.apache.velocity.tools.generic.LogTool());
            } else if ("math".equals(tool)) {
                velocityContext.put("math", new org.apache.velocity.tools.generic.MathTool());
            } else if ("number".equals(tool)) {
                velocityContext.put("number", new org.apache.velocity.tools.generic.NumberTool());
            } else if ("render".equals(tool)) {
                velocityContext.put("render", new org.apache.velocity.tools.generic.RenderTool());
            } else if ("text".equals(tool)) {
                velocityContext.put("text", new org.apache.velocity.tools.generic.ResourceTool());
            } else if ("xml".equals(tool)) {
                velocityContext.put("xml", new org.apache.velocity.tools.generic.XmlTool());
            }
        }
    }

}
