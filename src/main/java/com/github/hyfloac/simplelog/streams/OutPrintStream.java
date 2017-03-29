package com.github.hyfloac.simplelog.streams;

import com.github.hyfloac.simplelog.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.OutputStream;
import java.io.PrintStream;

public class OutPrintStream extends PrintStream
{
    protected transient int depth;

    public OutPrintStream(OutputStream out) { super(out); }

    public void outputln(@NotNull String s) { super.println(s); }

    @Override
    public void println(@NotNull String msg)
    {
        Logger.LOGGER.log(Logger.LogLevel.OUT, msg, depth - 1);
        depth = 0;
    }

    @Override
    public void println(@NotNull Object msg)
    {
        ++depth;
        println(msg.toString());
    }
}
