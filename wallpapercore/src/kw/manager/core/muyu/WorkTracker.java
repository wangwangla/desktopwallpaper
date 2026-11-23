package kw.manager.core.muyu;

import java.io.*;
import java.nio.file.*;
import java.time.*;
import java.util.*;

public final class WorkTracker {
    private final Path file;
    private LocalDate date;
    private LocalDateTime pausedFrom;
    private final List<LocalDateTime[]> pauses=new ArrayList<>();
    public WorkTracker(Path file){this.file=file;if(file==null)return;try(InputStream in=Files.newInputStream(file)){Properties p=new Properties();p.load(in);date=LocalDate.parse(p.getProperty("date"));String active=p.getProperty("paused","");if(!active.isEmpty())pausedFrom=LocalDateTime.parse(active);
        for(String range:p.getProperty("ranges","").split(";"))if(!range.isEmpty()){String[] pair=range.split("\\|");pauses.add(new LocalDateTime[]{LocalDateTime.parse(pair[0]),LocalDateTime.parse(pair[1])});}
    }catch(Exception e){date=null;pausedFrom=null;pauses.clear();}}
    private void roll(LocalDateTime now){if(!now.toLocalDate().equals(date)){date=now.toLocalDate();pausedFrom=null;pauses.clear();}}
    public boolean paused(){return pausedFrom!=null;}
    public void toggle(LocalDateTime now) throws IOException {roll(now);if(pausedFrom==null)pausedFrom=now;else{pauses.add(new LocalDateTime[]{pausedFrom,now});pausedFrom=null;}save();}
    public WorkSchedule.Snapshot snapshot(WorkSettings s,LocalDateTime now){roll(now);long excluded=0;for(LocalDateTime[] range:pauses)excluded+=WorkSchedule.productiveBetween(s,range[0],range[1]);if(pausedFrom!=null)excluded+=WorkSchedule.productiveBetween(s,pausedFrom,now);return WorkSchedule.at(s,now,excluded,paused());}
    private void save() throws IOException {if(file==null)return;Files.createDirectories(file.getParent());Properties p=new Properties();p.setProperty("date",date.toString());p.setProperty("paused",pausedFrom==null?"":pausedFrom.toString());StringBuilder ranges=new StringBuilder();for(LocalDateTime[] range:pauses)ranges.append(range[0]).append('|').append(range[1]).append(';');p.setProperty("ranges",ranges.toString());try(OutputStream out=Files.newOutputStream(file)){p.store(out,"Today work estimate pauses");}}
}
