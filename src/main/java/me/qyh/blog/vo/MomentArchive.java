package me.qyh.blog.vo;

import me.qyh.blog.entity.Moment;

import java.time.LocalDate;
import java.util.List;

public class MomentArchive {

    private final LocalDate date;
    private final List<Moment> moments;

    public MomentArchive(LocalDate date, List<Moment> moments) {
        super();
        this.date = date;
        this.moments = moments;
    }

    public LocalDate getDate() {
        return date;
    }

    public List<Moment> getMoments() {
        return moments;
    }

    @Override
    public String toString() {
        return "MomentArchive [date=" + date + ", moments=" + moments + "]";
    }

}
