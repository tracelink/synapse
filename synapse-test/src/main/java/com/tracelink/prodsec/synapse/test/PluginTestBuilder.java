package com.tracelink.prodsec.synapse.test;

import com.tracelink.prodsec.synapse.products.model.ProductLineModel;
import com.tracelink.prodsec.synapse.products.model.ProjectModel;
import com.tracelink.prodsec.synapse.scheduler.job.SchedulerJob;
import com.tracelink.prodsec.synapse.scheduler.service.schedule.ISchedule;
import com.tracelink.prodsec.synapse.scorecard.model.ScorecardColumn;
import com.tracelink.prodsec.synapse.scorecard.model.ScorecardValue;
import com.tracelink.prodsec.synapse.sidebar.model.SidebarLink;
import com.tracelink.prodsec.synapse.spi.PluginDisplayGroup;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.function.Function;

@SuppressWarnings("unchecked")
public class PluginTestBuilder<T extends PluginTestBuilder<T>> {

	protected PluginDisplayGroup pdg;
	protected final List<SchedulerJob> sjs = new ArrayList<>();
	protected final List<ScorecardColumn> scs = new ArrayList<>();
	protected final List<SidebarLink> sls = new ArrayList<>();
	protected final List<String> privs = new ArrayList<>();

	public T withDisplayGroup(String displayName, String materialIcon) {
		pdg = new PluginDisplayGroup(displayName, materialIcon);
		return (T) this;
	}

	public T withJobConfiguration(String jobName, boolean hasJob, ISchedule schedule) {
		sjs.add(new SchedulerJob() {

			@Override
			public String getJobName() {
				return jobName;
			}

			@Override
			public Runnable getJob() {
				if (hasJob) {
					return () -> {
					};
				} else {
					return null;
				}
			}

			@Override
			public ISchedule getSchedule() {
				return schedule;
			}
		});
		return (T) this;
	}

	public T withScorecardColumn(String columnName, String pageLink, boolean hasProjectCallback,
			boolean hasProductCallback) {
		scs.add(new ScorecardColumn() {
			@Override
			public Function<ProjectModel, ScorecardValue> getProjectCallbackFunction() {
				if (hasProjectCallback) {
					return (f) -> null;
				}
				return null;
			}

			@Override
			public Function<ProductLineModel, ScorecardValue> getProductLineCallbackFunction() {
				if (hasProductCallback) {
					return (f) -> null;
				}
				return null;
			}

			@Override
			public String getPageLink() {
				return pageLink;
			}

			@Override
			public String getColumnName() {
				return columnName;
			}
		});
		return (T) this;
	}

	public T withSidebarLink(String displayName, String[] authorizations, String pageLink,
			String materialIcon) {
		sls.add(new SidebarLink() {

			@Override
			public String getDisplayName() {
				return displayName;
			}

			@Override
			public String getAuthorizeExpression() {
				return null;
			}

			@Override
			public Collection<String> getAuthorizePrivileges() {
				if (authorizations == null) {
					return new HashSet<>();
				}
				return new HashSet<>(Arrays.asList(authorizations));
			}

			@Override
			public String getPageLink() {
				return pageLink;
			}

			@Override
			public String getMaterialIcon() {
				return materialIcon;
			}

		});
		return (T) this;
	}

	public T withPrivilege(String privilege) {
		privs.add(privilege);
		return (T) this;
	}
}
