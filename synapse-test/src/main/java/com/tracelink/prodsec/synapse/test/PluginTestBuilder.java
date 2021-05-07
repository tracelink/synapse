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

/**
 * Builder to test database plugins.
 *
 * @param <T> the type of the builder
 */
@SuppressWarnings("unchecked")
public class PluginTestBuilder<T extends PluginTestBuilder<T>> {

	private PluginDisplayGroup pdg;
	private final List<SchedulerJob> sjs = new ArrayList<>();
	private final List<ScorecardColumn> scs = new ArrayList<>();
	private final List<SidebarLink> sls = new ArrayList<>();
	private final List<String> privs = new ArrayList<>();

	/**
	 * Sets the display name and material icon of this builder and returns this.
	 *
	 * @param displayName  the name of the plugin
	 * @param materialIcon the material icon of the plugin
	 * @return this builder
	 */
	public T withDisplayGroup(String displayName, String materialIcon) {
		pdg = new PluginDisplayGroup(displayName, materialIcon);
		return (T) this;
	}

	/**
	 * Sets the scheduled jobs of this builder and returns this.
	 *
	 * @param jobName  the name of the job
	 * @param hasJob   whether the plugin has a job
	 * @param schedule the schedule object for when the job should run
	 * @return this builder
	 */
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

	/**
	 * Sets the scorecard column of this builder and returns this.
	 *
	 * @param columnName         the name of the scorecard column
	 * @param pageLink           the page the column redirects to
	 * @param hasProjectCallback boolean indicating if there is a project callback for the column
	 * @param hasProductCallback boolean indicating if there is a product callback for this column
	 * @return this builder
	 */
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

	/**
	 * Sets a sidebar link of this builder and returns this.
	 *
	 * @param displayName    the name of the sidebar link
	 * @param authorizations authorizations required to access the link
	 * @param pageLink       the link the sidebar should contain
	 * @param materialIcon   the material icon to be displayed on the link
	 * @return this builder
	 */
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

	/**
	 * Sets a privilege of this builder and returns this.
	 *
	 * @param privilege the name of the plugin privilege
	 * @return this builder
	 */
	public T withPrivilege(String privilege) {
		privs.add(privilege);
		return (T) this;
	}

	public PluginDisplayGroup getPdg() {
		return pdg;
	}

	public List<SchedulerJob> getSjs() {
		return sjs;
	}

	public List<ScorecardColumn> getScs() {
		return scs;
	}

	public List<SidebarLink> getSls() {
		return sls;
	}

	public List<String> getPrivs() {
		return privs;
	}
}
