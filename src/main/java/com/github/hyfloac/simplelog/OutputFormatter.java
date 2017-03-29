package com.github.hyfloac.simplelog;

import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * The outputFormat is used to determine how the output should look.
 * The elements in the outputFormat are:
 * <p>
 * <li>Timestamp Location: 'time'</li>
 * <li>Timestamp Style:
 * <li>Milliseconds: 'u'</li>
 * <li>Seconds: 's'</li>
 * <li>Minutes: 'm'</li>
 * <li>Hours: 'h'</li>
 * <li>Days: 'd'</li>
 * </li>
 * <li>Log Level Location: 'level'</li>
 * <li>Method that made the log call: 'caller'</li>
 * <li>The output msg: 'msg'</li>
 * </p>
 * <br />
 * <p>
 * Format Examples:
 * <li>"[time] [level] [caller]: msg"</li>
 * <li>"(time) level !!!caller!?! ---> msg!?!?!?...</li>
 * Time Examples:
 * <li>uu}ss>m{@literal <}h;dddddddd</li>
 * <li>ss:mm:hh</li>
 * <li>dd:mm:hh:ss:uuuu</li>
 * <li>dhmskun</li>
 * </p>
 */
public class OutputFormatter
{
    private static final Pattern TIMESTAMP = Pattern.compile("timestamp", Pattern.LITERAL);
    private static final Pattern TYPE = Pattern.compile("type", Pattern.LITERAL);
    private static final Pattern CALLER_CLASS = Pattern.compile("caller_class", Pattern.LITERAL);
    private static final Pattern MSG = Pattern.compile("msg", Pattern.LITERAL);
    private static final Pattern NON_TIMES = Pattern.compile("[^usmhd:]");
    private static final Pattern TIMES = Pattern.compile("(?<=[usmhdUSMHD])([usmhdUSMHD]+)");

    @NotNull
    private final String outputFormat;

    @NotNull
    /*private*/ public final Timer timer;

    public OutputFormatter(@NotNull("Passed a null value to parameter[0] at `com.github.hyfloac.simplelog.OutputFormatter(java.lang.String, java.lang.String") String outputFormat,
                           @NotNull("Passed a null value to parameter[0] at `com.github.hyfloac.simplelog.OutputFormatter(java.lang.String, java.lang.String") String timeFormat)
    {
        this.outputFormat = outputFormat;
        @NotNull int[] timeOrganizer = new int[5];
        /* { u, s, m, h, d } */
        for(char c : timeFormat.toCharArray())
        {
            if(c == 'u') { ++timeOrganizer[0]; }
            else if(c == 's') { ++timeOrganizer[1]; }
            else if(c == 'm') { ++timeOrganizer[2]; }
            else if(c == 'h') { ++timeOrganizer[3]; }
            else if(c == 'f') { ++timeOrganizer[4]; }
        }

        String tmp = TIMES.matcher(NON_TIMES.matcher(timeFormat).replaceAll("")).replaceAll("");
        String[] split = tmp.split(":");
        int[] timeOrder = new int[5];
        for(int i = 0; i < split.length; ++i)
        {
            if("u".equals(split[i])) { timeOrder[0] = i; }
            else if("s".equals(split[i])) { timeOrder[1] = i; }
            else if("m".equals(split[i])) { timeOrder[2] = i; }
            else if("h".equals(split[i])) { timeOrder[3] = i; }
            else if("f".equals(split[i])) { timeOrder[4] = i; }
        }

        /*
        for(int i = 0; i < timeOrganizer.length; ++i)
        {
            int index;
            switch(i)
            {
                case 0:
                    index = timeFormat.indexOf("u");
                    if(index == -1) { continue; }
                    timeFormat = new StringBuilder(timeFormat.substring(0, index + 1)).append(timeFormat.substring(index + 1, timeFormat.length()).replaceAll("u", "")).toString();
                    break;
                case 1:
                    index = timeFormat.indexOf("s");
                    if(index == -1) { continue; }
                    timeFormat = new StringBuilder(timeFormat.substring(0, index + 1)).append(timeFormat.substring(index + 1, timeFormat.length()).replaceAll("s", "")).toString();
                    break;
                case 2:
                    index = timeFormat.indexOf("m");
                    if(index == -1) { continue; }
                    timeFormat = new StringBuilder(timeFormat.substring(0, index + 1)).append(timeFormat.substring(index + 1, timeFormat.length()).replaceAll("m", "")).toString();
                    break;
                case 3:
                    index = timeFormat.indexOf("h");
                    if(index == -1) { continue; }
                    timeFormat = new StringBuilder(timeFormat.substring(0, index + 1)).append(timeFormat.substring(index + 1, timeFormat.length()).replaceAll("h", "")).toString();
                    break;
                case 4:
                    index = timeFormat.indexOf("d");
                    if(index == -1) { continue; }
                    timeFormat = new StringBuilder(timeFormat.substring(0, index + 1)).append(timeFormat.substring(index + 1, timeFormat.length()).replaceAll("d", "")).toString();
                    break;
            }
        }
        */

        timer = new Timer(timeOrganizer, timeOrder);
    }

    @NotNull
    public <T> String getPrintString(@NotNull String type, @NotNull String caller, @NotNull T msg) { return MSG.matcher(CALLER_CLASS.matcher(TYPE.matcher(TIMESTAMP.matcher(outputFormat).replaceAll(Matcher.quoteReplacement(timer.getTime()))).replaceAll(Matcher.quoteReplacement(type))).replaceAll(Matcher.quoteReplacement(caller))).replaceAll(Matcher.quoteReplacement(String.valueOf((Object) msg))); }

    public static class Timer
    {
        @NotNull
        private long[] times;

        @NotNull
        private long[] lastTimes;

        @NotNull
        private int[] timeOrganizer;

        @NotNull
        private String[] stringTimes;

        @NotNull
        private int[] timeOrder;

        /*private final*/ public long startTime;

        private Timer(@NotNull int[] timeOrganizer, @NotNull int[] timeOrder)
        {
            times = new long[5];
            lastTimes = new long[] { -1, -1, -1, -1, -1 };
            this.timeOrganizer = timeOrganizer;
            stringTimes = new String[5];
            this.timeOrder = timeOrder;
            startTime = System.currentTimeMillis();
        }

        @NotNull
        private String formatTime()
        {
            String[] outS = new String[5];
            for(int i = 0; i < timeOrganizer.length; ++i)
            {
                if(timeOrganizer[i] == 0) { continue; }
                if(times[i] == lastTimes[i]) { outS[timeOrder[i]] = stringTimes[i];/*out.append(stringTimes[i]);*/ }
                else { outS[timeOrder[i]] = stringTimes[i] = String.format(new StringBuilder("%0").append(timeOrganizer[i]).append("d").toString(), times[i]); }
            }
            StringBuilder builder = new StringBuilder();
            for(String s : outS)
            {
                if(s == null || s.isEmpty()) { continue; }
                builder.append(s).append(':');
            }
            String out0 = builder.toString();
            return out0.substring(0, out0.length() - 1);
        }

        @NotNull
        private String getTime()
        {
            long time = System.currentTimeMillis() - startTime;

            if(timeOrganizer[0] > 0) { times[0] = time % 1000; } //u
            time /= 1_000;

            if(timeOrganizer[1] > 0) { times[1] = time % 60; } //s
            time /= 60;

            if(timeOrganizer[2] > 0) { times[2] = time % 60; } //m
            time /= 60;

            if(timeOrganizer[3] > 0) { times[3] = time % 24; } //h
            if(timeOrganizer[4] > 0) { times[4] = time / 24; } //d
            return formatTime();
        }
    }
}
