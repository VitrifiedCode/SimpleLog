package com.github.hyfloac.simplelog;

import com.github.hyfloac.simplelog.streams.ErrPrintStream;
import com.github.hyfloac.simplelog.streams.FatalPrintStream;
import com.github.hyfloac.simplelog.streams.OutPrintStream;
import com.github.hyfloac.simplelog.streams.WarnPrintStream;
import org.fusesource.jansi.AnsiConsole;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;

@SuppressWarnings({ "WeakerAccess", "unused", "EmptyCatchBlock" })
public class Logger
{
    /* static block needs to be first to prevent a null pointer. */
    static
    {
        AnsiConsole.systemInstall();
        Runtime.getRuntime().addShutdownHook(new Thread(AnsiConsole::systemUninstall));
        System.setOut(new OutPrintStream(System.out));
        System.setErr(new ErrPrintStream(System.out));
        FATAL = new FatalPrintStream(System.out);
        WARN = new WarnPrintStream(System.out);
    }

    public static final Logger LOGGER = new Logger("default", "DefaultLogger");
    public static final Logger DEBUG = new Logger(true, "debug", "DefaultDebugLogger");
    public static Logger INSTANCE = LOGGER;


    protected static final PrintStream FATAL;
    protected static final PrintStream WARN;

    @Nullable
    protected BufferedWriter fileOutput;

    protected boolean debugMode;
    protected boolean shouldWriteLogToFile;

    @NotNull
    /*protected*/ public OutputFormatter formatter;

    @NotNull
    protected String name;

    private transient int tmpDepth;

    public boolean isWritingLogToFile() { return shouldWriteLogToFile; }

    public Logger() { this(false, ""); }

    public Logger(@NotNull("Passed a null value to parameter[0] at `com.github.hyfloac.simplelog.Logger(java.lang.String)`") String logFileDirectory) { this(false, logFileDirectory); }

    public Logger(@NotNull("Passed a null value to parameter[0] at `com.github.hyfloac.simplelog.Logger(java.lang.String)`") String logFileDirectory, @NotNull("Passed a null value to parameter[1] at `com.github.hyfloac.simplelog.Logger(java.lang.String, java.lang.String)`") String name) { this(false, logFileDirectory, name); }

    public Logger(boolean debug) { this(debug, ""); }

    public Logger(boolean debug, @NotNull("Passed a null value to parameter[1] at `com.github.hyfloac.simplelog.Logger(boolean, java.lang.String)`") String logFileDirectory) { this(debug, logFileDirectory, "[timestamp] [type] [caller_class]: msg", "hh:mm:ss", System.currentTimeMillis() + ""); }

    public Logger(boolean debug, @NotNull("Passed a null value to parameter[1] at `com.github.hyfloac.simplelog.Logger(boolean, java.lang.String)`") String logFileDirectory, @NotNull("Passed a null value to parameter[2] at `com.github.hyfloac.simplelog.Logger(boolean, java.lang.String, java.lang.String)`") String name) { this(debug, logFileDirectory, "[timestamp] [type] [caller_class]: msg", "hh:mm:ss", name); }

    public Logger(boolean debug,
                  @NotNull("Passed a null value to parameter[1] at `com.github.hyfloac.simplelog.Logger(boolean, java.lang.String, java.lang.String, java.lang.String, java.lang.String)`") String logDir,
                  @NotNull("Passed a null value to parameter[2] at `com.github.hyfloac.simplelog.Logger(boolean, java.lang.String, java.lang.String, java.lang.String, java.lang.String)`") String format,
                  @NotNull("Passed a null value to parameter[3] at `com.github.hyfloac.simplelog.Logger(boolean, java.lang.String, java.lang.String, java.lang.String, java.lang.String)`") String timeFormat,
                  @NotNull("Passed a null value to parameter[4] at `com.github.hyfloac.simplelog.Logger(boolean, java.lang.String, java.lang.String, java.lang.String, java.lang.String)`") String name)
    {
        Runtime.getRuntime().addShutdownHook(new Thread(this::close));
        formatter = new OutputFormatter(format, timeFormat);
        this.name = name;
        try
        {
            if(!logDir.isEmpty())
            {
                File f = new File(logDir);
                f.mkdirs();
                f = new File(new StringBuilder(logDir).append(logDir.endsWith("/") ? "" : "/").append("log_").append(System.currentTimeMillis() / 1000).append(".log").toString());
                if(!f.createNewFile()) { throw new IOException(); }
                fileOutput = new BufferedWriter(new FileWriter(f));
                shouldWriteLogToFile = true;
            }
        }
        catch(IOException e) { warn("Unable to create Log File."); }
        setDebugMode(debug || System.getProperty("debug") != null);
        tmpDepth = 0;
    }

    public void close()
    {
        try { fileOutput.close(); } catch(NullPointerException | IOException ignored) {}
    }

    public boolean isDebugMode() { return debugMode; }

    public void setDebugMode(boolean debugMode)
    {
        boolean tmp = this.debugMode;
        this.debugMode = debugMode;
        if(debugMode && !tmp) { debug("Logger enabling debug mode."); }
    }

    public void toggleDebugMode() { setDebugMode(!debugMode); }

    public <T> void log(@NotNull("Passed a null value to parameter[0] at `com.github.hyfloac.simplelog.Logger#log(com.github.hyfloac.simplelog.Logger$LogType, T, int") LogLevel level,
                                     @NotNull("Passed a null value to parameter[1] at `com.github.hyfloac.simplelog.Logger#log(com.github.hyfloac.simplelog.Logger.LogType, T, int") T msg, int depth)
    {
        if((level == LogLevel.DEBUG) && !debugMode) { return; }
        String out = formatter.getPrintString((name + ":" + level.toString()), Caller.getCallerClassName(depth + tmpDepth), msg);
        try { if(shouldWriteLogToFile) { fileOutput.write(out + '\n'); } } catch(Exception e) { shouldWriteLogToFile = false; }
        if(level.stream instanceof OutPrintStream) { ((OutPrintStream) level.stream).outputln(out); }
        else { level.stream.println(out); }
        level.stream.flush();
        tmpDepth = 0;
    }

    public <T> void log(@NotNull("Passed a null value to parameter[0] at `com.github.hyfloac.simplelog.Logger#<T>log(com.github.hyfloac.simplelog.Logger$LogType, T)") LogLevel type,
                        @NotNull("Passed a null value to parameter[1] at `com.github.hyfloac.simplelog.Logger#<T>log(com.github.hyfloac.simplelog.Logger.LogType, T)") T msg)
    { log(type, msg, 0); }


    public <T> void info(@NotNull("Passed a null value to parameter[0] at `com.github.hyfloac.simplelog.Logger#<T>info(T)`") T msg) { log(LogLevel.INFO, msg); }

    public <T> void warn(@NotNull("Passed a null value to parameter[0] at `com.github.hyfloac.simplelog.Logger#<T>warn(T)`") T msg) { log(LogLevel.WARN, msg); }

    public <T> void error(@NotNull("Passed a null value to parameter[0] at `com.github.hyfloac.simplelog.Logger#<T>error(T)`") T msg) { log(LogLevel.ERROR, msg); }

    public <T> void trace(@NotNull("Passed a null value to parameter[0] at `com.github.hyfloac.simplelog.Logger#<T>trace(T)`") T msg) { log(LogLevel.TRACE, msg); }

    public void trace(@NotNull("Passed a null value to parameter[0] at `com.github.hyfloac.simplelog.Logger#trace(java.lang.Exception)`") Exception e)
    {
        String stacktrace = build(false, "\n\tat ", e.getStackTrace());
        String eCast = e.toString();
        trace(new StringBuilder(eCast.length() + 5 + stacktrace.length()).append(eCast).append("\n\tat ").append(stacktrace).toString());
    }

    public <T> void trace(@NotNull("Passed a null value to parameter[0] at `com.github.hyfloac.simplelog.Logger#<T>trace(java.lang.Exception, T)`") Exception e,
                          @NotNull("Passed a null value to parameter[1] at `com.github.hyfloac.simplelog.Logger#<T>trace(java.lang.Exception, T)`") T msg)
    {
        if(!((String) msg).isEmpty()) { trace(msg); }
        trace(e);
    }

    public <T> void fatal(@NotNull("Passed a null value to parameter[0] at `com.github.hyfloac.simplelog.Logger#<T>fatal(T)`") T msg) { log(LogLevel.FATAL, msg); }

    public <T> void debug(@NotNull("Passed a null value to parameter[0] at `com.github.hyfloac.simplelog.Logger#<T>debug(T)`") T msg) { log(LogLevel.DEBUG, msg); }

    public <T> void out(@NotNull("Passed a null value to parameter[0] at `com.github.hyfloac.simplelog.Logger#<T>out(T)`") T msg) { log(LogLevel.OUT, msg); }

    public <T> void err(@NotNull("Passed a null value to parameter[0] at `com.github.hyfloac.simplelog.Logger#<T>err(T)`") T msg) { log(LogLevel.ERR, msg); }


    public static void closeS() { INSTANCE.close(); }

    public static boolean isDebugModeS() { return INSTANCE.isDebugMode(); }

    public static void setDebugModeS(boolean debugMode) { INSTANCE.setDebugMode(debugMode); }

    public static void toggleDebugModeS() { INSTANCE.toggleDebugMode(); }

    public static <T> void logS(LogLevel level, T msg, int depth) { ++INSTANCE.tmpDepth; INSTANCE.log(level, msg, depth); }

    public static <T> void logS(LogLevel type, T msg) { ++INSTANCE.tmpDepth; INSTANCE.log(type, msg); }

    public static <T> void infoS(T msg) { ++INSTANCE.tmpDepth; INSTANCE.info(msg); }

    public static <T> void warnS(T msg) { ++INSTANCE.tmpDepth; INSTANCE.warn(msg); }

    public static <T> void errorS(T msg) { ++INSTANCE.tmpDepth; INSTANCE.err(msg); }

    public static <T> void traceS(T msg) { ++INSTANCE.tmpDepth; INSTANCE.trace(msg); }

    public static void traceS(Exception e) { ++INSTANCE.tmpDepth; INSTANCE.trace(e); }

    public static <T> void traceS(Exception e, T msg) { ++INSTANCE.tmpDepth; INSTANCE.trace(e, msg); }

    public static <T> void fatalS(T msg) { ++INSTANCE.tmpDepth; INSTANCE.fatal(msg); }

    public static <T> void debugS(T msg) { ++INSTANCE.tmpDepth; INSTANCE.debug(msg); }

    public static <T> void outS(T msg) { ++INSTANCE.tmpDepth; INSTANCE.out(msg); }

    public static <T> void errS(T msg) { ++INSTANCE.tmpDepth; INSTANCE.err(msg); }


    /**
     * A way of concatenating Strings with StringBuilder in a smaller {@literal &} more optimized fashion (most people don't set the size)
     *
     * @param keepEnd Should the end of the string also contain the deliminator.
     * @param delim   A deliminator between Strings.
     * @param in      An array of strings to concatenate
     * @return The concatenated String
     */
    @SafeVarargs
    public static <T> String build(final boolean keepEnd, @NotNull final String delim, @NotNull final T... in)
    {
        if(in.length == 0) { return ""; }
        int length = 0;
        for(T s : in) { length += s.toString().length(); }
        StringBuilder sb = new StringBuilder(length + (delim.length() * in.length));
        for(T s : in) { sb.append(s).append(delim); }
        String out = sb.toString();
        if(!keepEnd) { out = out.substring(0, out.length() - delim.length()); }
        return out;
    }

    public enum LogLevel
    {
        INFO(System.out),
        WARN(Logger.WARN),
        ERROR(System.err),
        TRACE(System.err),
        FATAL(Logger.FATAL),
        DEBUG(System.out),
        OUT(System.out),
        ERR(System.err);

        public final PrintStream stream;

        LogLevel(PrintStream stream) { this.stream = stream; }
    }

    public static final class Caller
    {
        private Caller() {}

        private static final int BASE_DEPTH = 5;

        @NotNull("Error getting the stacktrace at `com.github.hyfloac.simplelog.Logger$Caller#getCallerClassName(int)")
        public static String getCallerClassName(int depth)
        {
            StackTraceElement[] elements = Thread.currentThread().getStackTrace();
            StackTraceElement element = elements[BASE_DEPTH + depth];
            if(element.getClassName().startsWith("kotlin.io.")) { element = elements[BASE_DEPTH + 2 + depth]; }
            else if(element.getClassName().startsWith("java.lang.Throwable")) { element = elements[BASE_DEPTH + 4 + depth]; }
            return element.getClassName();
        }

        @NotNull("Error getting the stacktrace at `com.github.hyfloac.simplelog.Logger$Caller#getCallerClassName()")
        public static String getCallerClassName() { return getCallerClassName(0); }
    }
}
