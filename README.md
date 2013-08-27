atom-ticket（DBMS通用SEQUENCE）
==========================================

### 配置简单（Spring配置）
	<!-- ===================================== -->
	<!-- 票据配置 -->
	<!-- ===================================== -->
	<bean id="parentMutexTicket" abstract="true">
		<property name="ticketDAO">
			<bean class="com.github.obullxl.ticket.support.DefaultTicketDAO">
				<property name="dataSource" ref="dataSource" />
				<property name="retryTimes" value="3" />
				<property name="step" value="3" />
				<property name="tableName" value="atom_mutex_ticket" />
				<property name="nameColumnName" value="name" />
				<property name="valueColumnName" value="value" />
				<property name="stampColumnName" value="stamp" />
			</bean>
		</property>
	</bean>

	<!-- ===================================== -->
	<!-- 用户ID配置 -->
	<!-- ===================================== -->
	<bean id="mutexTicket" class="com.github.obullxl.ticket.support.DefaultMutexTicket" parent="parentMutexTicket">
		<property name="name" value="SEQ-MUTEX-TICKET" />
	</bean>

### 使用简单
	@Autowired
    private MutexTicket ticket;

	// ... process 
	
	// 获取ID
	long id = ticket.nextValue();
	
	// ... other process

### 简单用法
    public void test_ticket() throws Exception {
        ApplicationContext context = new ClassPathXmlApplicationContext("classpath:/how-to-use.xml");
        MutexTicket ticket = context.getBean(MutexTicket.class);
        for (int i = 0; i < 100; i++) {
            System.out.println(ticket.nextValue());
        }
    }

### 建议或BUG
> 
> obullxl@gmail.com
> 
