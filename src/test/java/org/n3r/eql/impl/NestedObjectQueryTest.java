package org.n3r.eql.impl;

import org.junit.BeforeClass;
import org.junit.Test;
import org.n3r.eql.Eql;

import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class NestedObjectQueryTest {
    @BeforeClass
    public static void beforeClass() {
        new Eql("mysql").execute();
    }

    @Test
    public void test1() {
        List<CoachMe> coachMes = new Eql("mysql").returnType(CoachMe.class).execute();
        CoachMe coachMe = coachMes.get(0);
        assertThat(coachMe.getCoachId(), is(equalTo("123")));
        assertThat(coachMe.getCoach().getCoachId(), is(equalTo("123")));
    }

    public static class CoachMe {
        private String coachId;
        private Coach coach;

        public String getCoachId() {
            return coachId;
        }

        public void setCoachId(String coachId) {
            this.coachId = coachId;
        }

        public Coach getCoach() {
            return coach;
        }

        public void setCoach(Coach coach) {
            this.coach = coach;
        }
    }

    public static class Coach {
        private String coachId;

        public String getCoachId() {
            return coachId;
        }

        public void setCoachId(String coachId) {
            this.coachId = coachId;
        }
    }
}
