package com.company.service;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.company.config.i18n.MessageFactory;
import com.company.config.i18n.Messages;
import com.company.config.i18n.ServiceException;
import com.company.model.User;

@ActiveProfiles(profiles = "test")
@RunWith(SpringRunner.class)
@SpringBootTest
public class UserServiceTest {

	@Autowired
	private UserService service;

	@Rule
	public ExpectedException exceptionRule = ExpectedException.none();

	private User newTestObject() {
		User pojo = new User();
		return pojo;
	}

	@Test
	public void findAllTest() {
		// when
		List<User> list = this.service.findAll();
		// then
		assertNotNull(list);
	}

	@Test
	public void findByIdExceptionTest() {
		exceptionRule.expect(ServiceException.class);
		exceptionRule.expectMessage(MessageFactory.getMessage(Messages.record_not_found));
		// given
		Long id = null;
		// when
		this.service.findById(id);
		// then
		// throws ServiceException
	}

	@Test
	public void saveTest() {
		// given
		User pojo = this.newTestObject();
		// when
		pojo = this.service.save(pojo);
		// then
		assertNotNull(pojo.getId());
	}

	@Test
	public void logicalExclusionTest() {
		// given
		User pojo = this.newTestObject();
		pojo = this.service.save(pojo);
		// when
		this.service.logicalExclusion(pojo.getId());
		// then
		assertTrue(this.service.findAllDeleted().contains(pojo));
		assertFalse(this.service.findAll().contains(pojo));
	}

	@Test
	public void logicalExclusionExceptionTest() {
		exceptionRule.expect(ServiceException.class);
		exceptionRule.expectMessage(MessageFactory.getMessage(Messages.record_not_found));
		// given
		User pojo = new User();
		// when
		this.service.logicalExclusion(pojo.getId());
		// then
		// throws ServiceException
	}

	@Test
	public void findAllDeletedTest() {
		// given
		User pojo = this.service.save(this.newTestObject());
		this.service.logicalExclusion(pojo.getId());
		// when
		List<User> list = this.service.findAllDeleted();
		// then
		assertTrue(list.contains(pojo));
	}

	@Test
	public void restoreDeletedTest() {
		// given
		User pojo = this.service.save(this.newTestObject());
		this.service.logicalExclusion(pojo.getId());
		// when
		this.service.restoreDeleted(pojo.getId());
		// then
		assertTrue(this.service.findAll().contains(pojo));
		assertFalse(this.service.findAllDeleted().contains(pojo));
	}

	@Test
	public void restoreDeletedExceptionTest() {
		// given
		exceptionRule.expect(ServiceException.class);
		exceptionRule.expectMessage(MessageFactory.getMessage(Messages.record_not_found_at_recycle_bin));
		User pojo = new User();
		// when
		this.service.restoreDeleted(pojo.getId());
		// then
		// throws ServiceException
	}

	@Test
	public void permanentDestroyTest() {
		// given
		User pojo = this.service.save(this.newTestObject());
		this.service.logicalExclusion(pojo.getId());
		// when
		this.service.permanentDestroy(pojo.getId());
		// then
		assertFalse(this.service.findAll().contains(pojo));
		assertFalse(this.service.findAllDeleted().contains(pojo));
	}

	@Test
	public void permanentDestroyExceptionTest() {
		exceptionRule.expect(ServiceException.class);
		exceptionRule.expectMessage(MessageFactory.getMessage(Messages.record_not_found_at_recycle_bin));
		// given
		User pojo = new User();
		// when
		this.service.permanentDestroy(pojo.getId());
		// then
		// throws ServiceException
	}
}
