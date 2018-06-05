/*
 * Copyright 2018 qyh.me
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package me.qyh.blog.template.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import me.qyh.blog.template.entity.HistoryTemplate;

public interface HistoryTemplateDao {

	List<HistoryTemplate> selectByTemplateName(String templateName);

	void deleteByTemplateName(String templateName);

	void insert(HistoryTemplate template);

	void deleteById(Integer id);

	HistoryTemplate selectById(Integer id);

	void update(HistoryTemplate historyTemplate);

	void updateTemplateName(@Param("oldTemplateName") String oldTemplateName,
			@Param("newTemplateName") String newTemplateName);

}
