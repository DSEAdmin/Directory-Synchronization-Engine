/**********************BEGIN LICENSE BLOCK**************************************
 *   Version: MPL 1.1
 * 
 *  The contents of this file are subject to the Mozilla Public License Version
 *  1.1 (the "License"); you may not use this file except in compliance with
 *   the License. You may obtain a copy of the License at
 *   http://www.mozilla.org/MPL/
 * 
 *  Software distributed under the License is distributed on an "AS IS" basis,
 *  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 *  for the specific language governing rights and limitations under the
 *  License.
 * 
 *  The Original Code is the Directory Synchronization Engine(DSE).
 * 
 *  The Initial Developer of the Original Code is IronKey, Inc.
 *  Portions created by the Initial Developer are Copyright (C) 2011
 *  the Initial Developer. All Rights Reserved.
 * 
 *  Contributor(s): Shirish Rai
 * 
 ************************END LICENSE BLOCK*************************************/
package server.id.test;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.transaction.annotation.Transactional;


	

@Transactional
public class ServiceDAO {
    static Logger logger = Logger.getLogger(ServiceDAO.class.getName());
    private EntityManagerFactory emf;
    private EntityManager em;
    public ServiceDAO() {
    	ApplicationContext context = new ClassPathXmlApplicationContext("config/appTestContext.xml");
    	emf = (EntityManagerFactory) context.getBean("entityManagerFactory");
	    em = emf.createEntityManager();
	}
    
    @SuppressWarnings("unused")
	public List getUserLogs(int user_id){
  	  Query q = em.createNativeQuery("select ulog.create_date,ldef.name from user_log ulog, log_def ldef where ulog.user_id=?1 and ulog.log_def_id = ldef.log_def_id order by user_log_id desc limit 1");
  	  q.setParameter(1, user_id);

  	  List recs = q.getResultList();
  	  return recs;
    }
    
    public Account getAccountByName(String name) {
    	Query query = em.createQuery("SELECT acct from Account acct where acct.name = ?1");
		query.setParameter("1",name);
		try {			
			return (Account)query.getSingleResult();
		} catch(NoResultException e) {
			logger.debug(name + " account not found:" + e.toString());
		}
		return null;
    }
    @SuppressWarnings("unchecked")
	public List<User> getUserByUsername(Account account, String username) {
      Query query = em.createQuery("SELECT DISTINCT user " + 
				"FROM User user, UserToUserGroup userToUserGroup, Account account, UserStatusDef usd " + 
				"where user.username = ?1 and user.id = userToUserGroup.user.id and " + 
				"userToUserGroup.userGroup.account = ?2 and " +
      			"user.userStatusDef.shortName != 'deleted'");
      query.setParameter("1", username);
      query.setParameter("2", account);
      return (List<User>)query.getResultList();
    }
    public List<User> getUserByUsernameWithComponents(Account account, String username) {
        List<User> users = getUserByUsername(account, username);
        
        if (users == null) {
      	return null;
        }
        
        for(User u : users) {
      	u.getUserToRoles().size();
      	u.getUserInvites().size();
      	u.getRemoteObjects().size();
      	u.getUserPrefs().size();
        }
        
        return users;
    }
    
    @SuppressWarnings("unchecked")
	public List getGroupByName(String groupName){
  	  Query q = em.createNativeQuery("select role_id from role where name=?1");
  	  q.setParameter(1, groupName);

  	  List recs = q.getResultList();
  	  return recs;
    }
    
    public List getUserToRole(int userId, String groupName){
	  Query q = em.createNativeQuery("select utr.role_id,user_id from user_to_role utr, role r where utr.user_id = ?1 and utr.role_id=r.role_id and r.name=?2");
	  q.setParameter(1, userId);
	  q.setParameter(2, groupName);

	  List recs = q.getResultList();
	  return recs;
    }
    public List getUserInviteByUserName(String userName){
    	  Query q = em.createNativeQuery("select first,last,ui.email,usd.name,usd.short_name from user u, user_invite ui,user_status_def usd where u.username =?1 and u.user_id=ui.user_id and u.user_status_def_id=usd.user_status_def_id and usd.short_name='pending'");
    	  q.setParameter(1, userName);

    	  List recs = q.getResultList();
    	  return recs;
      }
}
