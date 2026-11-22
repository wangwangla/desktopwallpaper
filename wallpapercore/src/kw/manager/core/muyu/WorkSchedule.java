package kw.manager.core.muyu;

import java.time.*;
import java.time.temporal.ChronoUnit;

/** Income is a schedule-based estimate, never a bank balance or payroll record. */
public final class WorkSchedule {
    public static final class Snapshot {
        public String state, countdownLabel;
        public long countdown, worked, planned;
        public int restDays, paydayDays;
        public double earned, speed, monthly;
        public boolean paused;
    }
    public static long planned(WorkSettings s){return (s.end-s.start-Math.max(0,s.breakEnd-s.breakStart))*60L;}
    public static long productiveUntil(WorkSettings s,LocalDateTime now){
        if(!s.works(now.toLocalDate()))return 0;
        long second=now.toLocalTime().toSecondOfDay(),elapsed=Math.max(0,Math.min(second,s.end*60L)-s.start*60L);
        long lunch=Math.max(0,Math.min(second,s.breakEnd*60L)-s.breakStart*60L);return Math.max(0,elapsed-lunch);
    }
    public static long productiveBetween(WorkSettings s,LocalDateTime from,LocalDateTime to){return Math.max(0,productiveUntil(s,to)-productiveUntil(s,from));}
    public static Snapshot at(WorkSettings s,LocalDateTime now,long excluded,boolean paused){
        Snapshot v=new Snapshot();LocalDate date=now.toLocalDate();long seconds=now.toLocalTime().toSecondOfDay();v.planned=planned(s);v.paused=paused;
        v.worked=Math.max(0,Math.min(v.planned,productiveUntil(s,now)-excluded));v.earned=s.dailyIncome*v.worked/v.planned;
        boolean work=s.works(date),during=work&&seconds>=s.start*60L&&seconds<s.end*60L;
        boolean lunch=during&&seconds>=s.breakStart*60L&&seconds<s.breakEnd*60L;
        if(during){v.state=paused?"已暂停":lunch?"午间休息":"上班中";v.countdownLabel="距离下班还有";v.countdown=s.end*60L-seconds;}
        else if(work&&seconds<s.start*60L){v.state="还没上班";v.countdownLabel="距离上班还有";v.countdown=s.start*60L-seconds;}
        else{v.state=work?"下班啦":"休息时间";v.countdownLabel="距离下次上班";LocalDate next=date.plusDays(1);while(!s.works(next))next=next.plusDays(1);v.countdown=Duration.between(now,next.atStartOfDay().plusMinutes(s.start)).getSeconds();}
        v.speed=during&&!lunch&&!paused?s.dailyIncome/v.planned:0;
        v.restDays=-1;for(int i=0;i<8;i++)if(!s.works(date.plusDays(i))){v.restDays=i;break;}
        LocalDate pay=paydate(YearMonth.from(date),s.payday);if(pay.isBefore(date))pay=paydate(YearMonth.from(date).plusMonths(1),s.payday);
        v.paydayDays=(int)ChronoUnit.DAYS.between(date,pay);
        for(LocalDate d=date.withDayOfMonth(1);d.isBefore(date);d=d.plusDays(1))if(s.works(d))v.monthly+=s.dailyIncome;
        v.monthly+=v.earned;return v;
    }
    private static LocalDate paydate(YearMonth month,int day){return month.atDay(Math.min(day,month.lengthOfMonth()));}
    public static String duration(long seconds){long h=seconds/3600,m=seconds%3600/60,s=seconds%60;return String.format("%02d:%02d:%02d",h,m,s);}
}
