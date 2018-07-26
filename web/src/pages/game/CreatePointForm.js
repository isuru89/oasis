import React, { Component } from 'react'
import Form from "antd/lib/form";
import Input from "antd/lib/input";
import Radio from "antd/lib/radio";
import InputNumber from 'antd/lib/input-number';

const TextArea = Input.TextArea;

const formItemLayout = {
  labelCol: { span: 4 },
  wrapperCol: { span: 14 },
}

const formSubItemLayout = {
  labelCol: { span: 4 },
  wrapperCol: { span: 6 },
}


class PointForm extends Component {

  state = {
    awardPointType: 1
  }

  render() {
    const { onSubmit } = this.props;
    const { getFieldDecorator } = this.props.form;

    return (
      <Form onSubmit={onSubmit}>
        <Form.Item {...formSubItemLayout} label="Rule Name">
        {
          getFieldDecorator('name', {
            rules: [ { required: true, message: 'Rule name is mandatory', whitespace: false } ]
          })(<Input />)
        }
        </Form.Item>
        <Form.Item {...formItemLayout} label="Rule Display Text">
          {
            getFieldDecorator('displayName')(
              <Input />
            )
          }
        </Form.Item>
        <Form.Item {...formSubItemLayout} label="For Event">
          {
            getFieldDecorator('event', {
              rules: [ { required: true, message: 'Event type is mandatory', whitespace: false } ]
            })(<Input />)
          }
        </Form.Item>
        <Form.Item {...formItemLayout} label="Condition" 
          extra="Specify the boolean conditional expression to be evaluted for an event to award the points">
          <TextArea autosize={{ minRows: 4 }} />
        </Form.Item>
        <Form.Item {...formItemLayout} label="Award Points:">
          {
            getFieldDecorator('award-points')(
              <Radio.Group onChange={this._whenPointAwardTypeClicked} value={this.state.awardPointType}>
                <Radio value={1}>Constant Amount</Radio>
                <Radio value={2}>Expression</Radio>
              </Radio.Group>
            )
          }
        </Form.Item>
        {
          this.state.awardPointType === 1 ?
          <Form.Item {...formSubItemLayout} label="Points">
            {
              getFieldDecorator('amount')(
                <InputNumber />
              )
            }
          </Form.Item>
          :
          <Form.Item {...formItemLayout} label="Amount Expression" 
            extra="Specify the expression to calculate how many points going to be awarded using event values.">
            {
              getFieldDecorator('amount')(
                <TextArea autosize={{ minRows: 3 }} />
              )
            }
          </Form.Item>
        }
        <hr/>
        <h3 style={{paddingLeft: 20}}>Additional Points:</h3>
        <Form.Item {...formSubItemLayout} label="Event Name" 
            extra="Name of the point rule to be awarded for the other user.">
            <Input />
        </Form.Item>
        <Form.Item {...formSubItemLayout} label="User Field" 
            extra="Specify the user field reference of the event">
            <Input />
        </Form.Item>
        <Form.Item {...formSubItemLayout} label="Point Amount" 
            extra="Expression or constant points to be awarded for delegated user.">
            {
              getFieldDecorator('delegate-amount')(
                <TextArea autosize={{ minRows: 1 }} />
              )
            }
        </Form.Item>
      </Form>
    )
  }

  _whenPointAwardTypeClicked = e => {
    this.setState({ awardPointType: e.target.value });
  }
}

export const CreatePointForm = Form.create()(PointForm)
