package kw.manager.core.muyu;

import java.io.*;
import java.nio.file.*;
import java.util.Properties;

public final class WorkSettings {
    public boolean visible = true, configured = false;
    public int weekdays = 31, start = 540, end = 1080, breakStart = 720, breakEnd = 810, payday = 10;
    public double dailyIncome = 200;
    public String title = "上班鼓励", pet = "cat", petName = "团子";
    public int textColor = 0x473b30, backgroundColor = 0xfffcf5;

    public static Path file() {
        return MuyuProgress.defaultFile().resolveSibling("work-settings.properties");
    }

    public WorkSettings copy() {
        WorkSettings s = new WorkSettings();
        s.visible = visible;
        s.configured = configured;
        s.weekdays = weekdays;
        s.start = start;
        s.end = end;
        s.breakStart = breakStart;
        s.breakEnd = breakEnd;
        s.payday = payday;
        s.dailyIncome = dailyIncome;
        s.title = title;
        s.pet = pet;
        s.petName = petName;
        s.textColor = textColor;
        s.backgroundColor = backgroundColor;
        return s;
    }

    public boolean works(java.time.LocalDate date) {
        return (weekdays & (1 << (date.getDayOfWeek().getValue() - 1))) != 0;
    }

    public void validate() {
        if (weekdays < 1 || weekdays > 127)
            throw new IllegalArgumentException("至少选择一个工作日");
        if (start < 0 || end > 1440 || end <= start)
            throw new IllegalArgumentException("下班时间需要晚于上班时间，当前支持当天班次");
        if (breakStart != breakEnd && (breakStart < start || breakEnd > end || breakEnd < breakStart))
            throw new IllegalArgumentException("休息时间需要位于工作时间内");
        if (!Double.isFinite(dailyIncome) || dailyIncome < 0 || dailyIncome > 1_000_000_000)
            throw new IllegalArgumentException("每日收入需要为有效的非负金额");
        if (end - start - Math.max(0, breakEnd - breakStart) <= 0)
            throw new IllegalArgumentException("有效工时不能为零");
        if (payday < 1 || payday > 31)
            throw new IllegalArgumentException("发薪日需要在 1 到 31 日之间");
        if (title.trim().isEmpty() || title.length() > 20 || petName.trim().isEmpty() || petName.length() > 12)
            throw new IllegalArgumentException("组件名称限 1～20 字，宠物名称限 1～12 字");
    }

    public static WorkSettings load() {
        WorkSettings s = new WorkSettings();
        try (InputStream in = Files.newInputStream(file())) {
            Properties p = new Properties();
            p.load(in);
            s.visible = Boolean.parseBoolean(p.getProperty("visible", "true"));
            s.configured = Boolean.parseBoolean(p.getProperty("configured", "false"));
            s.weekdays = Integer.parseInt(p.getProperty("weekdays", "31"));
            s.start = Integer.parseInt(p.getProperty("start", "540"));
            s.end = Integer.parseInt(p.getProperty("end", "1080"));
            s.breakStart = Integer.parseInt(p.getProperty("breakStart", "720"));
            s.breakEnd = Integer.parseInt(p.getProperty("breakEnd", "810"));
            s.payday = Integer.parseInt(p.getProperty("payday", "10"));
            s.dailyIncome = Double.parseDouble(p.getProperty("dailyIncome", "200"));
            s.title = p.getProperty("title", s.title);
            s.pet = p.getProperty("pet", s.pet);
            s.petName = p.getProperty("petName", s.petName);
            s.textColor = Integer.parseInt(p.getProperty("textColor", Integer.toString(s.textColor)));
            s.backgroundColor = Integer.parseInt(p.getProperty("backgroundColor", Integer.toString(s.backgroundColor)));
            s.validate();
        } catch (Exception e) {
            return new WorkSettings();
        }
        return s;
    }

    public void save() throws IOException {
        validate();
        Properties p = new Properties();
        p.setProperty("visible", Boolean.toString(visible));
        p.setProperty("configured", Boolean.toString(configured));
        p.setProperty("weekdays", Integer.toString(weekdays));
        p.setProperty("start", Integer.toString(start));
        p.setProperty("end", Integer.toString(end));
        p.setProperty("breakStart", Integer.toString(breakStart));
        p.setProperty("breakEnd", Integer.toString(breakEnd));
        p.setProperty("payday", Integer.toString(payday));
        p.setProperty("dailyIncome", Double.toString(dailyIncome));
        p.setProperty("title", title);
        p.setProperty("pet", pet);
        p.setProperty("petName", petName);
        p.setProperty("textColor", Integer.toString(textColor));
        p.setProperty("backgroundColor", Integer.toString(backgroundColor));
        Files.createDirectories(file().getParent());
        Path temp = file().resolveSibling("work-settings.tmp");
        try (OutputStream out = Files.newOutputStream(temp)) {
            p.store(out, "Work encouragement settings");
        }
        Files.move(temp, file(), StandardCopyOption.REPLACE_EXISTING);
    }
}
