from typing import Tuple
import sqlite3
import re
import os
import argparse


def tanyoudb_field_to_cnnvd(col_name: str):
    """
    根据探优数据库中字段名返回cnnvd中的字段名
    :param col_name:
    :return:
    """
    dic = {
        'cnnvd_no': 'vuln_id',
        'vul_no': 'cve_id',
        'name': 'name',
        'severity': 'severity',
        'vul_type': 'vuln_type',
        'source': 'source',
        'description': 'vuln_descript',
        'date_exposure': 'modified_time',
        'date_created': 'published_time'
    }
    return dic.get(col_name)


def format_data_for_db(col_name: str, cnnvd_name_val: str, db_val: str) -> Tuple[str, str]:
    """
    格式化需要对的数据，比如危险等级，时间等
    :param col_name:
    :param cnnvd_name_val:
    :param db_val:
    :return:
    """
    # 如果是危险等级需要格式化成数字
    if col_name == 'severity':
        # 格式化危险等级
        if cnnvd_name_val != '':
            if '超危' in cnnvd_name_val:
                cnnvd_name_val = '1'
            elif '高危' in cnnvd_name_val:
                cnnvd_name_val = '2'
            elif '中危' in cnnvd_name_val:
                cnnvd_name_val = '3'
            elif '低危' in cnnvd_name_val:
                cnnvd_name_val = '4'
        else:
            cnnvd_name_val = '-1'
        db_val = str(db_val)
    # 如果是时间,转格式直接返回
    if col_name == 'date_exposure' or col_name == 'date_created':
        cnnvd_name_val = '''to_timestamp('{0} 00:00:00','yyyy-MM-DD hh24:mi:ss')'''.format(cnnvd_name_val)
        db_val = '''to_timestamp('{0}','yyyy-MM-DD hh24:mi:ss')'''.format(db_val)
    # 所有字符串转义 '->'' 并且 赋值需要（）括起来
    if isinstance(cnnvd_name_val,
                  str) and col_name != 'severity' and col_name != 'date_exposure' and col_name != 'date_created':
        cnnvd_name_val = cnnvd_name_val.replace('\'', '\'\'')
    if isinstance(db_val,
                  str) and col_name != 'severity' and col_name != 'date_exposure' and col_name != 'date_created':
        db_val = db_val.replace('\'', '\'\'')

    return cnnvd_name_val, db_val


def row_update_sql_bycveid(result):
    """
    根据字典信息，提取字段信息，生成单行更新SQL语句
    :param result: 字典的一行数据
    :return: 单行update SQL 语句
    """
    # 从行中提取信息
    pattern = '(?<=@h&lname@h&l:).*(?=@l&hname@l&h)'
    res = re.search(pattern, result)
    name = '' if res is None else res.group()
    pattern = '(?<=@h&lvuln_id@h&l:).*(?=@l&hvuln_id@l&h)'
    res = re.search(pattern, result)
    vuln_id = '' if res is None else res.group()
    pattern = '(?<=@h&lpublished_time@h&l:).*(?=@l&hpublished_time@l&h)'
    res = re.search(pattern, result)
    published_time = '' if res is None else res.group()
    pattern = '(?<=@h&lmodified_time@h&l:).*(?=@l&hmodified_time@l&h)'
    res = re.search(pattern, result)
    modified_time = '' if res is None else res.group()
    pattern = '(?<=@h&lseverity@h&l:).*(?=@l&hseverity@l&h)'
    res = re.search(pattern, result)
    severity = '' if res is None else res.group()
    pattern = '(?<=@h&lvuln_type@h&l:).*(?=@l&hvuln_type@l&h)'
    res = re.search(pattern, result)
    vuln_type = '' if res is None else res.group()
    pattern = '(?<=@h&lvuln_descript@h&l:).*(?=@l&hvuln_descript@l&h)'
    res = re.search(pattern, result)
    vuln_descript = '' if res is None else res.group()
    pattern = '(?<=@h&lcve_id@h&l:).*(?=@l&hcve_id@l&h)'
    res = re.search(pattern, result)
    cve_id = '' if res is None else res.group()
    source = 'https://www.cnnvd.org.cn/home/globalSearch?keyword=' + vuln_id

    # 数据处理
    # 修正severity
    if '超危' in severity:
        severity = '1'
    elif '高危' in severity:
        severity = '2'
    elif '中危' in severity:
        severity = '3'
    elif '低危' in severity:
        severity = '4'
    else:
        severity = '-1'
    # 所有字符串都转义' 单引号改成2个单引号 '->''  并用（）把字符串括起来
    name = name.replace('\'', '\'\'')
    vuln_type = vuln_type.replace('\'', '\'\'')
    vuln_descript = vuln_descript.replace('\'', '\'\'')
    # 如果数据有空 预警并打印信息
    if name == '' or \
            vuln_id == '' or \
            published_time == '' or \
            modified_time == '' or \
            severity == '' or \
            vuln_type == '' or \
            vuln_descript == '' or \
            cve_id == '' or \
            source == '':
        print(cve_id + "的数据中有空！")
    # 生成修改db数据的SQL
    sql_result = '''UPDATE cnnvd_info
        SET cnnvd_no = '{0}',
            name = ('{1}'),
            severity = {2},
            vul_type = ('{3}'),
            source = '{4}',
            description = ('{5}'),
            date_exposure = to_timestamp('{6} 00:00:00','yyyy-MM-DD hh24:mi:ss'),
            date_created = to_timestamp('{7} 00:00:00','yyyy-MM-DD hh24:mi:ss')
        WHERE vul_no = '{8}';
    '''.format(vuln_id, name, severity, vuln_type, source, vuln_descript, modified_time, published_time,
               cve_id)
    # print(sql_result)
    return sql_result


def row_update_sql_byid(result, id):
    """
    根据字典信息，提取字段信息，生成单行更新SQL语句
    :param id: 数据库中id编号
    :param result: 字典的一行数据
    :return: 单行update SQL 语句
    """
    # 从行中提取信息
    pattern = '(?<=@h&lname@h&l:).*(?=@l&hname@l&h)'
    res = re.search(pattern, result)
    name = '' if res is None else res.group()
    pattern = '(?<=@h&lvuln_id@h&l:).*(?=@l&hvuln_id@l&h)'
    res = re.search(pattern, result)
    vuln_id = '' if res is None else res.group()
    pattern = '(?<=@h&lpublished_time@h&l:).*(?=@l&hpublished_time@l&h)'
    res = re.search(pattern, result)
    published_time = '' if res is None else res.group()
    pattern = '(?<=@h&lmodified_time@h&l:).*(?=@l&hmodified_time@l&h)'
    res = re.search(pattern, result)
    modified_time = '' if res is None else res.group()
    pattern = '(?<=@h&lseverity@h&l:).*(?=@l&hseverity@l&h)'
    res = re.search(pattern, result)
    severity = '' if res is None else res.group()
    pattern = '(?<=@h&lvuln_type@h&l:).*(?=@l&hvuln_type@l&h)'
    res = re.search(pattern, result)
    vuln_type = '' if res is None else res.group()
    pattern = '(?<=@h&lvuln_descript@h&l:).*(?=@l&hvuln_descript@l&h)'
    res = re.search(pattern, result)
    vuln_descript = '' if res is None else res.group()
    pattern = '(?<=@h&lcve_id@h&l:).*(?=@l&hcve_id@l&h)'
    res = re.search(pattern, result)
    cve_id = '' if res is None else res.group()
    source = 'https://www.cnnvd.org.cn/home/globalSearch?keyword=' + vuln_id

    # 数据处理
    # 修正severity
    if '超危' in severity:
        severity = '1'
    elif '高危' in severity:
        severity = '2'
    elif '中危' in severity:
        severity = '3'
    elif '低危' in severity:
        severity = '4'
    else:
        severity = '-1'
    # 所有字符串都转义' 单引号改成2个单引号 '->''  并用（）把字符串括起来
    name = name.replace('\'', '\'\'')
    vuln_type = vuln_type.replace('\'', '\'\'')
    vuln_descript = vuln_descript.replace('\'', '\'\'')
    # 如果数据有空 预警并打印信息
    if name == '' or \
            vuln_id == '' or \
            published_time == '' or \
            modified_time == '' or \
            severity == '' or \
            vuln_type == '' or \
            vuln_descript == '' or \
            cve_id == '' or \
            source == '':
        print(cve_id + "的数据中有空！")
    # 生成修改db数据的SQL
    sql_result = '''UPDATE cnnvd_info
        SET vul_no = '{0}',
            name = ('{1}'),
            severity = {2},
            vul_type = ('{3}'),
            source = '{4}',
            description = ('{5}'),
            date_exposure = to_timestamp('{6} 00:00:00','yyyy-MM-DD hh24:mi:ss'),
            date_created = to_timestamp('{7} 00:00:00','yyyy-MM-DD hh24:mi:ss')
        WHERE id = {8};
    '''.format(cve_id, name, severity, vuln_type, source, vuln_descript, modified_time, published_time,
               id)
    # print(sql_result)
    return sql_result


def modify_col_in_db(path: str, col_name: str, dic_path: str):
    """
    连接path路径下数据库，获得字段名，并根据字段名，修改数据库 或 生成sql 语句
    :param dic_path: 字典路径
    :param col_name: 对齐的数据库字段名
    :param path:  数据库路径
    :return: 为空
    """
    # 判断修改字段col_name对应CNNVD数据库中的字段
    cnnvd_name = tanyoudb_field_to_cnnvd(col_name)
    if cnnvd_name is None:
        print("探优数据中字段名输入错误！")
        return

    # 打开cnnvd爬取修改的字典信息文本
    file = open(dic_path, 'r')
    content = file.read()
    file.close()

    # CVE编号找不到的写入路径：
    # 因为后面模式为追加写入,每次运行都会追加数据，所以文件存在则删除，生成新的。
    cve_notfound_fpath = r"cve_notfound.txt"
    if os.path.exists(cve_notfound_fpath):
        os.remove(cve_notfound_fpath)
        print("cve_notfound.txt文件已存在，删除！")

    # 记录单条CVE编号对应多条字典数据的信息写入路径
    # 因为后面模式为追加写入,每次运行都会追加数据，所以文件存在则删除，生成新的。
    cve_to_multidata_fpath = r"cve_multidata.txt"
    if os.path.exists(cve_to_multidata_fpath):
        os.remove(cve_to_multidata_fpath)
        print("cve_multidata.txt文件已存在，删除！")

    # 计数器
    need_modify_nums = 0  # 计数器：数据库需要修改行数
    cve_notfound_nums = 0  # 计数器：数据库漏洞名在cnnvd下载数据中找不到的
    data_notfound_nums = 0  # 计数器：官方下载生成的字典信息中查找到的数据为空
    cve_to_multidata_nums = 0  # 计数器：记录单条CVE编号对应多条字典数据情况出现数量

    sql_when = ''  # 修改的sql语句拼接字符串1
    sql_where = '('  # 修改的sql语句拼接字符串2

    # 数据库连接
    conn = sqlite3.connect(path)
    cursor = conn.cursor()
    sql_str = 'select vul_no,{0},cnnvd_no,name, severity,vul_type,source,description,date_exposure,date_created from cnnvd_info;'.format(
        col_name)  # SQL的查找命令
    cursor.execute(sql_str)  # 执行SQL语句
    # 开始比遍历数据库中查询的到的漏洞名
    for item in cursor:
        # 先计算匹配数量
        match_num = content.count("@h&lcve_id@h&l:" + item[0] + "@l&hcve_id@l&h")
        # 字典中找不到
        if match_num <= 0:
            with open(cve_notfound_fpath, 'a') as f:
                f.write(item[0] + '\n')
            cve_notfound_nums = cve_notfound_nums + 1  # 未找到计数器+1
            continue
        # 单条CVE编号对应多条字典数据情况
        if match_num > 1:
            pattern = '@h&lname@h&l:.*@h&lcve_id@h&l:' + item[0] + '@l&hcve_id@l&h'
            res = re.findall(pattern, content)
            cve_to_multidata_nums = cve_to_multidata_nums + 1  # 计数器+1
            # 数据库中信息写入
            val_in_db = 'cve编号:' + item[0] + '\n'
            field_names = ['cnnvd编号:', 'name:', 'severity:', 'vul_type:', 'source:', 'description:', 'date_exposure:',
                           'date_created:']
            for i in range(2, len(item)):
                val_in_db = val_in_db + field_names[i - 2] + str(item[i]) + '\n'
            with open(cve_to_multidata_fpath, 'a') as ft:
                ft.write(val_in_db + '\n')
                # 每一条数据均生成一条对探优数据库表中整行update的单行SQL语句，并记录数据库中和字典文件中对应的信息
                # 目前SQL是通过ID去修正数据库，因为数量少，暂时不生成SQL
                i = 1
                for result in res:
                    row_sql = row_update_sql_bycveid(result)
                    ft.write('第' + str(i) + '个在字典中匹配到生成的SQL：\n' + row_sql + '\n')
                    i = i + 1
                ft.write('-------------------\n')
            continue
        # 单条CVE编号对应单条
        result = ''
        if match_num == 1:
            index = content.find('@h&lcve_id@h&l:' + item[0] + '@l&hcve_id@l&h')  # 在字典中查找CVE
            if index != -1:
                # 字典中找到，获取当前行
                start_index = content.rfind('@h&lname@h&l:', 0, index)
                end_index = content.find('@l&hcve_id@l&h\n', index) + len('@l&hcve_id@l&h')
                result = content[start_index: end_index]
            else:
                print("运行错误！！！！！")
        # 从行中提取信息
        pattern = '(?<=@h&l{0}@h&l:).*(?=@l&h{0}@l&h)'.format(cnnvd_name)
        res = re.search(pattern, result)
        cnnvd_name_val = '' if res is None else res.group()
        # 字典中没有找到提示, source字段字典文件没有，是拼接的值，跳过
        if cnnvd_name_val == '' and cnnvd_name != 'source':
            print(item[0] + "的" + cnnvd_name + "没有找到！！！！")
            data_notfound_nums = data_notfound_nums + 1

        # 核对数据中和cnnvd中是否一致
        # 格式化对比的数据
        cnnvd_name_val, db_val = format_data_for_db(col_name, cnnvd_name_val, item[1])
        # 如果是source,拼接url
        if col_name == 'source':
            base_url = r"https://www.cnnvd.org.cn/home/globalSearch?keyword="
            # 从字典中获取CNNVD编号
            pattern = '(?<=@h&lvuln_id@h&l:).*(?=@l&hvuln_id@l&h)'
            res = re.search(pattern, result)
            # 拼接
            cnnvd_name_val = base_url + res.group()
        # 数据不一致
        if cnnvd_name_val != db_val:
            # 不同字段不同处理
            # 时间字段和severity危险等级字段不是字符串，不需要加’’
            if col_name == 'date_exposure' or col_name == 'date_created' or col_name == 'severity':
                when = '''        WHEN '{0}' THEN {1}'''.format(item[0], cnnvd_name_val)
                sql_where = sql_where + '\'' + item[0] + '\', '
                sql_when += when + '\n        '
            else:
                when = '''        WHEN '{0}' THEN (\'{1}\')'''.format(item[0], cnnvd_name_val)
                sql_where = sql_where + '\'' + item[0] + '\', '
                sql_when += when + '\n        '
            need_modify_nums = need_modify_nums + 1  # 需修改计数器+1

    print(col_name + '需要修改：' + str(need_modify_nums))
    print('字典中没找到：' + str(cve_notfound_nums))
    print('字典中信息为空：' + str(data_notfound_nums))
    print('单条CVE编号对应多条字典数据情况数量：' + str(cve_to_multidata_nums))

    # 单条更新漏洞危险等级的sql语句格式化：合并
    sql_when = sql_when[:sql_when.rfind('\n')]
    sql_where = sql_where[:sql_where.rfind(',')] + ')'
    sql_result = '''UPDATE cnnvd_info
        SET {2}= CASE vul_no
        {0}
        END
    WHERE vul_no IN {1};
    '''.format(sql_when, sql_where, col_name)
    # print(sql_result)
    # 写如文件：单条更新漏洞危险等级的sql语句
    with open(col_name + '_sql.txt', 'w') as f:
        f.write(sql_result)

    # 数据库事务提交和连接关闭
    conn.commit()
    cursor.close()
    conn.close()
    return


def modify_cve_in_db(path: str, dic_path: str):
    """
    当数据库中CVE编号异常或者CVE在字典中找不到的时候，
    用cnnvd去反过来校验cve，
    并生成整行update的sql语句
    :param path: 数据库路径
    :param dic_path: 字典路径
    :return:
    """
    # 打开cnnvd爬取修改的字典信息文本
    file = open(dic_path, 'r')
    content = file.read()
    file.close()

    # CVE编号异常的写入路径：
    # 因为后面模式为追加写入,每次运行都会追加数据，所以文件存在则删除，生成新的。
    cve_error_fpath = r"CVE_ERROR.txt"
    if os.path.exists(cve_error_fpath):
        os.remove(cve_error_fpath)
        print("CVE_ERROR.txt文件存在已删除！")

    # CNNVD反查结果生成SQL的写入路径：
    # 因为后面模式为追加写入,每次运行都会追加数据，所以文件存在则删除，生成新的。
    modify_cve_fpath = r"CNNVD反查CVE的SQL.txt"
    if os.path.exists(modify_cve_fpath):
        os.remove(modify_cve_fpath)
        print("CNNVD反查CVE的SQL.txt文件存在已删除！")

    # 需要删除db中一行的SQL和对应的数据库中数据的写入路径：
    # case1: CVE和CNNVD编号都异常
    # case2: CVE和CNNVD都找不到
    delete_row_sql_path = 'delete_row_sql.txt'

    # 存放需要删除行在数据库中得id
    need_del_id = {}

    # 计算器
    need_del_nums = 0  # 计算器：需要删除的行的数量
    cnnvd_notfound_nums = 0  # 计数器：数据库CNNVD编号在cnnvd下载数据中找不到的
    cve_notfound_nums = 0  # 计数器：数据库CVE编号在cnnvd下载数据中找不到的
    cnnvd_modify_cve_nums = 0  # 计数器：CNNVD反查CVE成功的计数器

    # 数据库连接
    conn = sqlite3.connect(path)
    cursor = conn.cursor()
    sql_str = 'select vul_no, cnnvd_no, id, name, severity,vul_type,source,description,date_exposure,date_created  from cnnvd_info;'  # SQL的查找命令
    cursor.execute(sql_str)  # 执行SQL语句
    for item in cursor:
        cve_id = item[0]
        cnnvd_id = item[1]
        db_id = item[2]
        name = item[3]
        severity = item[4]
        vul_type = item[5]
        source = item[6]
        description = item[7]
        date_exposure = item[8]
        date_created = item[9]
        # 判断CVE编号是否合法
        pattern = r'^CVE-\d{4}-\d{1,}$'
        res = re.search(pattern, cve_id)
        # CVE编号错误
        if res is None:
            f = open(cve_error_fpath, 'a')
            f.write(cve_id + '\n')
            # 判断cnnvd编号是否合法
            pattern = r'^CNNVD-\d{6}-\d{1,}$'
            res = re.search(pattern, cnnvd_id)
            # CNNVD编号错误
            if res is None:
                # cve和cnnvd都不合法，删除
                need_del_nums = need_del_nums + 1
                # 记录需要删除的行在数据库中的id和数据
                need_del_id[db_id] = str('数据库中CVE编号：' + cve_id + '\n'
                                         + '数据库中CNNVD编号：' + cnnvd_id + '\n'
                                         + '数据库中name：' + str(name) + '\n'
                                         + '数据库中severity：' + str(severity) + '\n'
                                         + '数据库中vul_type：' + str(vul_type) + '\n'
                                         + '数据库中source：' + str(source) + '\n'
                                         + '数据库中description：' + str(description) + '\n'
                                         + '数据库中date_exposure：' + str(date_exposure) + '\n'
                                         + '数据库中date_created：' + str(date_created))
            else:
                # cve不合法 cnnvd合法，cnnvd作为关键词索引
                keyword = cnnvd_id
                # 在字典信息中查找对应的keyword
                index = content.find('@h&lvuln_id@h&l:' + keyword + '@l&hvuln_id@l&h')
                if index != -1:
                    # 反查成功 计数器+1
                    cnnvd_modify_cve_nums = cnnvd_modify_cve_nums + 1
                    # 找到，获取当前行
                    start_index = content.rfind('@h&lname@h&l:', 0, index)
                    end_index = content.find('@l&hcve_id@l&h\n', index) + len('@l&hcve_id@l&h')
                    result = content[start_index: end_index]
                    # 生成单行update的SQL语句
                    row_sql = row_update_sql_byid(result, db_id)
                    with open(modify_cve_fpath, 'a') as f:
                        f.write(row_sql + '\n')
                else:
                    # 没有找到
                    cnnvd_notfound_nums = cnnvd_notfound_nums + 1  # 未找到计数器+1
                    # cve 不合法，CNNVD合法 但是找不到
                    need_del_nums = need_del_nums + 1
                    # 记录需要删除的行的ID和数据
                    need_del_id[db_id] = str('数据库中CVE编号：' + cve_id + '\n'
                                             + '数据库中CNNVD编号：' + cnnvd_id + '\n'
                                             + '数据库中name：' + str(name) + '\n'
                                             + '数据库中severity：' + str(severity) + '\n'
                                             + '数据库中vul_type：' + str(vul_type) + '\n'
                                             + '数据库中source：' + str(source) + '\n'
                                             + '数据库中description：' + str(description) + '\n'
                                             + '数据库中date_exposure：' + str(date_exposure) + '\n'
                                             + '数据库中date_created：' + str(date_created))
        else:
            # cve 合法 但是 cve找不到的
            # 计算匹配数量为0的用cnnvd查找
            match_num = content.count("@h&lcve_id@h&l:" + cve_id + "@l&hcve_id@l&h")
            if match_num < 1:
                # CVE正常但是在字典中没有找到的计数器+1
                cve_notfound_nums = cve_notfound_nums + 1
                # 查找CNNVD
                index = content.find('@h&lvuln_id@h&l:' + cnnvd_id + '@l&hvuln_id@l&h')
                if index != -1:
                    # 反查成功 计数器+1
                    cnnvd_modify_cve_nums = cnnvd_modify_cve_nums + 1
                    # 找到，获取当前行
                    start_index = content.rfind('@h&lname@h&l:', 0, index)
                    end_index = content.find('@l&hcve_id@l&h\n', index) + len('@l&hcve_id@l&h')
                    result = content[start_index: end_index]
                    # 生成单行update的SQL语句
                    row_sql = row_update_sql_byid(result, db_id)
                    with open(modify_cve_fpath, 'a') as f:
                        f.write(row_sql + '\n')
                else:
                    # CNNVD也没有找到
                    cnnvd_notfound_nums = cnnvd_notfound_nums + 1  # 未找到计数器+1
                    # cve 合法， 但是CNNVD找不到
                    need_del_nums = need_del_nums + 1
                    # 记录需要删除的行的ID和数据
                    need_del_id[db_id] = str('数据库中CVE编号：' + cve_id + '\n'
                                             + '数据库中CNNVD编号：' + cnnvd_id + '\n'
                                             + '数据库中name：' + str(name) + '\n'
                                             + '数据库中severity：' + str(severity) + '\n'
                                             + '数据库中vul_type：' + str(vul_type) + '\n'
                                             + '数据库中source：' + str(source) + '\n'
                                             + '数据库中description：' + str(description) + '\n'
                                             + '数据库中date_exposure：' + str(date_exposure) + '\n'
                                             + '数据库中date_created：' + str(date_created))

    # 写入删除一行的SQL
    if len(need_del_id) > 0:
        with open(delete_row_sql_path, 'w') as f:
            # 开头写入说明文件
            f.write('说明：下面显示的是探优数据库中一行的关键数据\n满足以下条件：\n'
                    '该行数据的CVE编号在字典中匹配不到，并且该行的CNNVD编号也在字典中匹配不到\n'
                    '----------开始----------\n')
            for del_id, val in need_del_id.items():
                # del_sql = 'DELETE FROM cnnvd_info WHERE id = {0};'.format(del_id)
                # f.write(val + '\n' + del_sql + '\n--------------------\n')
                f.write(val + '\n--------------------\n')

    print("CVE编号正常，但是字典找不到的数量：" + str(cve_notfound_nums))
    print("CNNVD没有匹配到的数量" + str(cnnvd_notfound_nums))
    print("CNNVD反查CVE成功生成的update单行sql的数量：" + str(cnnvd_modify_cve_nums))
    print("需要删除行的数量：" + str(need_del_nums))


if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument('option', type=int, default='')
    parser.add_argument('--db_path', type=str, default='')
    parser.add_argument('--col_name', type=str, default='')
    parser.add_argument('--dic_path', type=str, default='')
    args = parser.parse_args()
    if args.option == 1:
        # cve编号为keyword,根据探优数据库字段生成SQL语句修正 探优数据库中与cvvnd中不一致的数据
        # col_name为探优数据中的字段，在tanyouDB_field_to_cnnvd方法中对应,选项如下：
        # cnnvd_no、name、severity、vul_type、source、description、date_exposure、date_created
        db_path = args.db_path
        col_name = args.col_name
        dic_path = args.dic_path
        modify_col_in_db(db_path, col_name, dic_path)
    if args.option == 2:
        # CVE编号异常或者找不到的情况下，用CNNVD去反查这行数据。如果有生成update单行的SQL
        db_path = args.db_path
        dic_path = args.dic_path
        modify_cve_in_db(db_path, dic_path)

    pass
