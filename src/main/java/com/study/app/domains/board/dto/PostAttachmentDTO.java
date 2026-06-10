package com.study.app.domains.board.dto;

import java.time.LocalDate;

public class PostAttachmentDTO {
	

    private Long post_id;
    private String file_name;
    private String file_path;
    private Long file_size;
    private String file_type;
  

    public PostAttachmentDTO() {}


	public PostAttachmentDTO(Long post_id, String file_name, String file_path, Long file_size, String file_type) {
		super();
		this.post_id = post_id;
		this.file_name = file_name;
		this.file_path = file_path;
		this.file_size = file_size;
		this.file_type = file_type;
	}


	public Long getPost_id() {
		return post_id;
	}


	public void setPost_id(Long post_id) {
		this.post_id = post_id;
	}


	public String getFile_name() {
		return file_name;
	}


	public void setFile_name(String file_name) {
		this.file_name = file_name;
	}


	public String getFile_path() {
		return file_path;
	}


	public void setFile_path(String file_path) {
		this.file_path = file_path;
	}


	public Long getFile_size() {
		return file_size;
	}


	public void setFile_size(Long file_size) {
		this.file_size = file_size;
	}


	public String getFile_type() {
		return file_type;
	}


	public void setFile_type(String file_type) {
		this.file_type = file_type;
	}


    

}
