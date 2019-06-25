package com.stackroute.buzzup.schedularservice.service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;

import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger.TriggerState;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.stackroute.buzzup.kafka.model.MovieSchedule;
import com.stackroute.buzzup.schedularservice.job.JobFunction;

@Service
public class ListnerService {

	//Listning to the Kafka topic and recieving the object of MovieSchedule
	@KafkaListener(topics = "screening-details1", groupId = "scheduler", containerFactory = "kafkaListenerContainerFactory")
	public void consumeKafka(MovieSchedule scheduledMovie) throws ParseException, SchedulerException {

		String startDateStr = scheduledMovie.getMovieReleaseDate();
		String theatreName = scheduledMovie.getTheatreName();
		String showSlot = scheduledMovie.getShowTimings();
		String city = scheduledMovie.getTheatreCity();
		String[] showSlots = showSlot.split(",");
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
		String date = startDateStr;
		Calendar c = Calendar.getInstance();

		SimpleDateFormat dateformatter = new SimpleDateFormat("dd/MM/yyyy");
		Date dateStart = dateformatter.parse(startDateStr);
		c.setTime(dateStart);
		c.add(Calendar.DATE, 7);
		Date endDate = c.getTime();
		System.out.println(endDate);

		LocalDate localDate = LocalDate.parse(date, formatter);
		Scheduler sc = null;

		//running a loop for the number of shows per day
		for (int i = 0; i < showSlots.length; i++) {
			//converting string into time of format hours:minutes
			LocalTime localTime = LocalTime.parse(showSlots[i], DateTimeFormatter.ofPattern("HH:mm"));
			LocalDateTime a = LocalDateTime.of(localDate, localTime);
			Date d = Date.from(a.atZone(ZoneId.systemDefault()).toInstant());
			sc = StdSchedulerFactory.getDefaultScheduler();
			
			//JobKey will uniquely identify a Job
			JobKey jobKey = JobKey.jobKey("job" + i + theatreName + LocalDateTime.now());
			
			//create a job for JobFunction class and providing it with details
			JobDetail job = JobBuilder.newJob(JobFunction.class).usingJobData("theatreName", theatreName)
					.usingJobData("slot", showSlots[i]).usingJobData("city", city).withIdentity(jobKey).build();
			
			//TriggerKey for uniquely identifying a trigger
			TriggerKey triggerKey = TriggerKey.triggerKey("trigger" + i + theatreName + LocalDateTime.now());
			if (sc.getTriggerState(triggerKey) == TriggerState.ERROR) {
				sc.resumeTrigger(triggerKey);
			}
			
			// Executing a trigger at the given show times
			SimpleTrigger trigger = TriggerBuilder.newTrigger().forJob(job).withIdentity(triggerKey)
					.withDescription("Sample trigger").startAt(d).withSchedule(SimpleScheduleBuilder.simpleSchedule())
					.endAt(endDate).build();

			sc.scheduleJob(job, trigger);

		}
		sc.start();
	}
}