package kw.manager.core.seasons;

import java.time.*;
import java.util.concurrent.ConcurrentHashMap;

/** Apparent solar longitude crossings, with each term taking effect on its local calendar day. */
public final class SolarTerms {
    public static final String[] NAMES = {"立春","雨水","惊蛰","春分","清明","谷雨","立夏","小满","芒种","夏至","小暑","大暑",
        "立秋","处暑","白露","秋分","寒露","霜降","立冬","小雪","大雪","冬至","小寒","大寒"};
    public static final String[] SEASONS = {"春","夏","秋","冬"};
    public static final String[] VERSES = {"东风解冻，草木初萌","雨润新芽，春水渐生","桃始华，万物醒","昼夜均，花满枝","风清景明，芳草新绿","雨生百谷，春色将阑",
        "绿荫初成，蝉声未起","小得盈满，麦穗渐丰","芒种忙种，仲夏始来","日长之至，绿意正浓","温风始至，荷香渐远","盛夏流光，万物繁茂",
        "凉风有信，一叶知秋","暑气渐退，云淡风轻","露从今夜白，草木染秋","昼夜平分，金风送爽","露寒叶红，秋水澄明","霜染层林，岁晚流金",
        "水始冰，万物收藏","虹藏不见，天地初寒","雪覆枝头，静候春归","长夜尽处，一阳初生","岁寒深处，静听风声","冬将尽，春可期"};
    private static final ConcurrentHashMap<Integer, Instant[]> CACHE = new ConcurrentHashMap<>();
    private SolarTerms() {}

    public static Instant instant(int year, int index) {
        if (index < 0 || index >= 24) throw new IllegalArgumentException("Invalid solar term");
        return CACHE.computeIfAbsent(year, SolarTerms::calculate)[index];
    }
    public static LocalDate date(int year, int index, ZoneId zone) { return instant(year, index).atZone(zone).toLocalDate(); }
    public static int current(LocalDate day, ZoneId zone) {
        int result = 21;
        LocalDate latest = date(day.getYear()-1, 21, zone);
        for (int i=0;i<24;i++) {
            LocalDate start = date(day.getYear(), i, zone);
            if (!start.isAfter(day) && start.isAfter(latest)) { latest=start; result=i; }
        }
        return result;
    }
    public static int season(int term) { return term < 18 ? term/6 : 3; }
    public static LocalDate next(LocalDate day, ZoneId zone) {
        LocalDate next=day.plusYears(1);
        for (int y=day.getYear(); y<=day.getYear()+1; y++) for(int i=0;i<24;i++) {
            LocalDate d=date(y,i,zone); if(d.isAfter(day)&&d.isBefore(next)) next=d;
        }
        return next;
    }
    private static Instant[] calculate(int year) {
        Instant[] result=new Instant[24];
        for(int i=0;i<24;i++) {
            int month=i>=22?1:i/2+2;
            long low=LocalDate.of(year,month,1).atStartOfDay(ZoneOffset.UTC).toEpochSecond()-86400;
            long high=low+32*86400L;
            double target=(315+i*15)%360;
            for(int n=0;n<40 && high-low>1;n++) {
                long mid=(low+high)/2;
                double difference=(longitude(mid)-target+540)%360-180;
                if(difference<0)low=mid;else high=mid;
            }
            result[i]=Instant.ofEpochSecond(high);
        }
        return result;
    }
    // Meeus low-order apparent Sun formula; modern-year crossings are approximate to minutes.
    private static double longitude(long seconds) {
        double t=(2440587.5+seconds/86400.0-2451545)/36525;
        double l=280.46646+36000.76983*t+.0003032*t*t;
        double m=Math.toRadians(357.52911+35999.05029*t-.0001537*t*t);
        double c=(1.914602-.004817*t-.000014*t*t)*Math.sin(m)+(.019993-.000101*t)*Math.sin(2*m)+.000289*Math.sin(3*m);
        double apparent=l+c-.00569-.00478*Math.sin(Math.toRadians(125.04-1934.136*t));
        return (apparent%360+360)%360;
    }
}
