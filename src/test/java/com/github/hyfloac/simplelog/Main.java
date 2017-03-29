package com.github.hyfloac.simplelog;

public class Main
{
    public static void main(String[] args) throws Exception
    {
//        int r, g, b;
//        int x = 0;
//        for(int i = 0; i < Math.pow(255, 3) * 2; ++i)
//        {
//            int y = x;
//            r = y % 256;
//            y /= 256;
//            g = y % 256;
//            y /= 256;
//            b = y % 256;
//            y /= 256;
//            Logger.infoS("x: " + x + "; r: " + r + "; g: " + g + "; b: " + b);
//            ++x;
//        }

        Logger log = new Logger(true, "bla", "[timestamp] [type] [caller_class]: msg", "hh:mm:ss", "Test");

        log.info("Hi");
        log.warn("Uh, oh.");
        log.error("This is bad.");
        log.trace("This is really bad.");
        log.fatal("AAAHHHHHHHH");
        log.debug("You can't see me.");
        log.debug("Now you can see me.");
        log.formatter.timer.startTime -= 3429638609L;
        log.out("The world hates you.");
        log.err("The world hates you too.");

        System.out.println("The world really hates you.");
        System.err.println("The world really hates you too.");
    }
}
