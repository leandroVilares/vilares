package com.ml.logistica.romaneio.services;

import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;
import static javax.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;

import java.beans.PropertyEditorSupport;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.boot.context.embedded.FilterRegistrationBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ml.logistica.romaneio.dao.PackingListDAO;
import com.ml.logistica.romaneio.dao.PackingListFilter;
import com.ml.logistica.romaneio.entity.PackingList;
import com.ml.logistica.romaneio.entity.PackingListType;

@RestController
@RequestMapping(value = "/api/v1/packingLists")
@ConfigurationProperties(prefix = "ml.logistica.api.romaneio.service")
public class PackingListService {

	private static final int MAX_DAIS_BETWEEN_DATES = 60;
	private static final int DEFAULT_DAYS_PERIOD = 2;
	private static final String DATE_TIME_FORMAT = "yyyy-MM-dd hh:mm:ss";

	private static Logger logger = LoggerFactory.getLogger(PackingListService.class);

	@Autowired
	private PackingListDAO dao;

	private String authToken;

	private static int ERROR_COUNTER = 0;

	/**
	 * Create a java.util.Date binder to inject string date into java.util.Date
	 * object.
	 * 
	 * @param binder
	 */
	@InitBinder
	public void initBinder(WebDataBinder binder) {
		SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_TIME_FORMAT);
		binder.registerCustomEditor(Date.class, new CustomDateEditor(dateFormat, false));

		binder.registerCustomEditor(PackingListType.class, new PropertyEditorSupport() {
			@Override
			public void setAsText(String text) throws IllegalArgumentException {
				setValue(PackingListType.valueOf(NumberUtils.toLong(text, 0l)));
			}
		});
	}

	@Bean
	public FilterRegistrationBean registerAuthFilter() {
		FilterRegistrationBean registration = new FilterRegistrationBean();
		registration.setFilter(new Filter() {
			@Override
			public void init(FilterConfig filterConfig) throws ServletException {
			}

			@Override
			public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
					throws IOException, ServletException {
				HttpServletRequest req = (HttpServletRequest) request;
				if (StringUtils.trimToEmpty(req.getHeader("X-Auth")).equalsIgnoreCase(getAuthToken())) {
					chain.doFilter(request, response);
				} else {
					((HttpServletResponse) response).sendError(SC_UNAUTHORIZED);
					return;
				}
			}

			@Override
			public void destroy() {
			}
		});
		registration.addUrlPatterns("/api/packingList/*");
		registration.setName("PackingListServiceFilter");
		// registration.setOrder(1);
		return registration;

	}

	/**
	 * Find traditional packing lists.
	 * 
	 * @param startDate
	 * @param endtDate
	 * @param branch
	 * @return Returns a list of matching packing list
	 * @throws IOException
	 */
	@RequestMapping(method = RequestMethod.GET)
	public List<PackingList> findTraditionalPackingList(
			// --
			@RequestParam(value = "type.id", required = false) PackingListType type,
			// --
			@RequestParam(value = "branch.id", required = false) Long branch,
			// --
			@RequestParam(value = "id", required = false) Long id,
			// --
			@RequestParam(value = "createdat_greater", required = false) Date startDate,
			// --
			@RequestParam(value = "createdat_lesser", required = false) Date endtDate,
			// --
			HttpServletResponse resp) throws IOException {
		try {
			PackingListFilter filter = new PackingListFilter();

			/* ::: Validations ::: */

			// To query by ID brach mus be provided
			if (id != null && branch == null) {
				resp.sendError(SC_INTERNAL_SERVER_ERROR, "Branch is needed to query packing list by id.");
				return null;
			}

			// Fill dates if null.
			if (id == null) {
				startDate = fillStartDate(startDate, endtDate);
				endtDate = fillEndDate(startDate, endtDate);

				// --

				// Date interval can't be greater than a certain number of days.
				Long days = Duration.between(startDate.toInstant(), endtDate.toInstant()).toDays();

				if (days < 0) {
					resp.sendError(SC_INTERNAL_SERVER_ERROR, "The end date must be greater than start date.");
					return null;
				}
				if (days > MAX_DAIS_BETWEEN_DATES) {
					resp.sendError(SC_INTERNAL_SERVER_ERROR,
							"Date interval can't be greater than " + MAX_DAIS_BETWEEN_DATES + " months.");
					return null;
				}
			}

			/* ::: Fill filter object ::: */
			filter.setBranch(branch);
			filter.setId(id);
			if (startDate != null) {
				filter.setStartDate(DateUtils.toCalendar(startDate));
			}
			if (endtDate != null) {
				filter.setEndDate(DateUtils.toCalendar(endtDate));
			}

			/* ::: Query data ::: */
			List<PackingList> result = new ArrayList<>();
			if (type == null || PackingListType.Traditional.equals(type)) {
				List<PackingList> l = dao.findTraditionalPackingList(filter);
				if (l != null) {
					result.addAll(l);
				}
			}
			if (type == null || PackingListType.Courrier.equals(type)) {
				List<PackingList> l = dao.findCourrierPackingList(filter);
				if (l != null) {
					result.addAll(l);
				}
			}

			/* ::: Validate Result ::: */
			if (result == null || result.size() == 0) {
				resp.setStatus(SC_NOT_FOUND);
			}
			// --

			return result;

		} catch (Exception e) {
			String errorId = "PL-" + System.currentTimeMillis() + (++ERROR_COUNTER);
			logger.error(errorId + ": " + e.getMessage(), e);
			resp.sendError(SC_INTERNAL_SERVER_ERROR, "Internal server error. Error id is " + errorId);
			return null;
		}
	}

	private Date fillEndDate(Date startDate, Date endtDate) {
		if (endtDate == null) {
			if (startDate != null) {
				endtDate = DateUtils.addDays(startDate, DEFAULT_DAYS_PERIOD);
			} else {
				endtDate = new Date();
			}
		}
		return endtDate;
	}

	private Date fillStartDate(Date startDate, Date endtDate) {
		if (startDate == null) {
			if (endtDate == null) {
				startDate = DateUtils.addDays(new Date(), (DEFAULT_DAYS_PERIOD * -1));
			} else {
				startDate = DateUtils.addDays(endtDate, (DEFAULT_DAYS_PERIOD * -1));
			}
		}
		return startDate;
	}

	public String getAuthToken() {
		return authToken;
	}

	public void setAuthToken(String authToken) {
		this.authToken = authToken;
	}

}
