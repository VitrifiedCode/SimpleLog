package com.github.hyfloac.simplelog.streams;

import com.github.hyfloac.simplelog.Logger;
import org.fusesource.jansi.Ansi;
import org.jetbrains.annotations.NotNull;

import java.io.OutputStream;
import java.io.PrintStream;

public class ErrPrintStream extends OutPrintStream
{
    public ErrPrintStream(PrintStream out) { super(out); }

    @Override
    public void outputln(@NotNull String s) { super.outputln(Ansi.ansi().fg(Ansi.Color.RED).a(s).reset().toString()); }

    @Override
    public void println(@NotNull String msg)
    {
        Logger.LOGGER.log(Logger.LogLevel.ERR, msg, depth - 1);
        depth = 0;
    }
}
