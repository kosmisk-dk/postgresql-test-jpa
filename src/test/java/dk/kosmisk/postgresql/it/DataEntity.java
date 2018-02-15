/*
 * Copyright (C) 2018 Source (source (at) kosmisk.dk)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package dk.kosmisk.postgresql.it;

import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

/**
 *
 * @author Source (source (at) kosmisk.dk)
 */
@Entity
@Table(name = "data")
public class DataEntity implements Serializable {

    private static final long serialVersionUID = 433535877793034117L;

    @Id
    @NotNull
    private Integer key;

    @NotNull
    private String data;

    public DataEntity() {
    }

    public DataEntity(int key, String data) {
        this.key = key;
        this.data = data;
    }

    public int getKey() {
        return key;
    }

    public void setKey(int key) {
        this.key = key;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "DataEntity{" + "key=" + key + ", data=" + data + '}';
    }

}
