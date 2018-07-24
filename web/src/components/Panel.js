import React, { Component } from 'react'
import styled from 'styled-components';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'

const Wrapper = styled.div`
  margin: 5px;
  background-color: white;
  border-radius: 5px;
  box-shadow: 1px 1px 2px 2px #739CAE33;
`

const Expander = styled.span`
  color: #739CAE;
  opacity: 0.5;
  cursor: pointer;
  min-width: 20px;
  text-align: center;

  &:hover {
    opacity: 1;
  }
`

const Title = styled.span`
  flex: 1;
`

const TitleBar = styled.div`
  padding: 5px;
  margin: 0 5px;
  border-radius: 5px 5px 0 0;
  border-bottom: 1px solid #739CAE88;
  margin-bottom: 5px;
  display: flex;
  background-color: white;
`

const Content = styled.div`
  padding: 5px;
  border-radius: 0 0 5px 5px;
  border-top: none;
  background-color: white;
`

export default class Panel extends Component {

  state = {
    expanded: true
  }

  render() {
    const { expanded } = this.state;

    return (
      <Wrapper>
        <TitleBar>
          <Title>{this.props.title}</Title>
          <Expander onClick={this._whenExpandClicked}>
            {
              expanded ? <FontAwesomeIcon icon="caret-down" /> : <FontAwesomeIcon icon="caret-right" />
            }
          </Expander>
        </TitleBar>
        <Content>
          {expanded && this.props.children}
        </Content>
      </Wrapper>
    )
  }

  _whenExpandClicked = e => this.setState({ expanded: !this.state.expanded });
}
