package com.github.hyfloac.simplelog.streams;

import org.fusesource.jansi.Ansi;
import org.jetbrains.annotations.NotNull;

import java.io.OutputStream;
import java.io.PrintStream;

public class WarnPrintStream extends PrintStream
{
    public WarnPrintStream(OutputStream out) { super(out); }

    @Override
    public void println(@NotNull String msg) { super.println(Ansi.ansi().fg(Ansi.Color.YELLOW).a(msg).reset()); }
}
